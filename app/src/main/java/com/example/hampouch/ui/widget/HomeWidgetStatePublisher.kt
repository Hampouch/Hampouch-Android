package com.example.hampouch.ui.widget

import android.content.Context
import android.util.Log
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.RestState
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.domain.repository.RestRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "HomeWidgetPublisher"
private const val STATE_PUBLISH_DEBOUNCE_MILLIS = 300L

fun interface HomeWidgetRefreshRequester {
    fun refreshAfterExpenseChange()
}

@Singleton
class HomeWidgetStatePublisher @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val challengeRepository: ChallengeRepository,
    private val expenseRepository: ExpenseRepository,
    private val restRepository: RestRepository,
    private val snapshotStore: HomeWidgetSnapshotStore
) : HomeWidgetRefreshRequester {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val publishMutex = Mutex()
    private val serverSyncMutex = Mutex()
    private var started = false
    private var observedAccount: String? = null
    private var hadActiveChallenge = false

    @OptIn(FlowPreview::class)
    fun start() {
        if (started) return
        started = true
        scope.launch {
            combine(
                authRepository.userSession,
                challengeRepository.state,
                expenseRepository.records,
                restRepository.restState
            ) { session, challengeState, records, restState ->
                WidgetSourceState(session?.userId?.toString(), challengeState, records.values, restState)
            }.debounce(STATE_PUBLISH_DEBOUNCE_MILLIS).collect { source ->
                val error = runCatching { publish(source) }.exceptionOrNull() ?: return@collect
                if (error is CancellationException) throw error
                Log.e(TAG, "위젯 상태 갱신에 실패했습니다.", error)
            }
        }
    }

    suspend fun publishSessionStarted() {
        val accountKey = authRepository.userSession.first()?.userId?.toString()
        if (accountKey == null) {
            publishLoggedOut()
            return
        }
        publishMutex.withLock {
            snapshotStore.markSession(accountKey)
            observedAccount = accountKey
            hadActiveChallenge = false
        }
        HomeWidgetSyncScheduler.enqueueNow(context)
    }

    suspend fun requestImmediateSync() {
        if (!syncFromServer(forceWidgetUpdate = true)) {
            Log.w(TAG, "수동 위젯 동기화 중 일부 서버 조회에 실패했습니다.")
        }
    }

    override fun refreshAfterExpenseChange() {
        scope.launch { requestImmediateSync() }
    }

    internal suspend fun syncFromServer(forceWidgetUpdate: Boolean = false): Boolean = serverSyncMutex.withLock {
        if (authRepository.userSession.first() == null) {
            publishLoggedOut(forceWidgetUpdate)
            return@withLock true
        }

        val result = runCatching {
            val restResult = restRepository.syncStatus()
            val challengeResult = challengeRepository.loadCurrentChallenge()
            val expenseResult = expenseRepository.loadDay(LocalDate.now())
            if (authRepository.userSession.first() == null) {
                publishLoggedOut(forceWidgetUpdate)
                true
            } else {
                publishAfterHomeSync(
                    noActiveConfirmed = challengeResult.isSuccess,
                    forceWidgetUpdate = forceWidgetUpdate
                )
                restResult.isSuccess && challengeResult.isSuccess && expenseResult.isSuccess
            }
        }
        val error = result.exceptionOrNull()
        if (error is CancellationException) throw error
        if (error != null) {
            Log.e(TAG, "위젯 서버 동기화에 실패했습니다.", error)
        }
        result.getOrDefault(false)
    }

    suspend fun publishAfterHomeSync(
        noActiveConfirmed: Boolean = true,
        forceWidgetUpdate: Boolean = false
    ) {
        val session = authRepository.userSession.first()
        publish(
            source = WidgetSourceState(
                accountKey = session?.userId?.toString(),
                challengeState = challengeRepository.state.value,
                records = expenseRepository.records.value.values,
                restState = restRepository.restState.value
            ),
            noActiveConfirmed = noActiveConfirmed,
            forceWidgetUpdate = forceWidgetUpdate
        )
    }

    suspend fun publishLoggedOut(forceWidgetUpdate: Boolean = false) {
        publishMutex.withLock {
            if (authRepository.userSession.first() != null) return@withLock
            publishLoggedOutLocked(forceWidgetUpdate)
        }
    }

    private suspend fun publishLoggedOutLocked(forceWidgetUpdate: Boolean = false) {
        observedAccount = null
        hadActiveChallenge = false
        snapshotStore.publishLoggedOut(forceWidgetUpdate)
    }

    private suspend fun publish(
        source: WidgetSourceState,
        noActiveConfirmed: Boolean = false,
        forceWidgetUpdate: Boolean = false
    ) =
        publishMutex.withLock {
            val currentAccount = authRepository.userSession.first()?.userId?.toString()
            if (source.accountKey != currentAccount) return@withLock
            publishLocked(source, noActiveConfirmed, forceWidgetUpdate)
        }

    private suspend fun publishLocked(
        source: WidgetSourceState,
        noActiveConfirmed: Boolean,
        forceWidgetUpdate: Boolean
    ) {
        val accountKey = source.accountKey
        if (accountKey == null) {
            publishLoggedOutLocked(forceWidgetUpdate)
            return
        }
        if (observedAccount != accountKey) {
            snapshotStore.markSession(accountKey)
            observedAccount = accountKey
            hadActiveChallenge = false
        }

        val resting = source.restState as? RestState.Resting
        if (resting != null) {
            snapshotStore.publishResting(accountKey, resting.plannedResumeDate, forceWidgetUpdate)
            return
        }

        val today = LocalDate.now()
        val challenge = source.challengeState.challengeFor(today)
        if (challenge != null) {
            hadActiveChallenge = true
            val todaySpent = source.records.filter { it.date == today }.sumOf { it.amount }
            snapshotStore.publishChallenge(accountKey, challenge, todaySpent, forceWidgetUpdate)
        } else if (hadActiveChallenge || noActiveConfirmed) {
            hadActiveChallenge = false
            snapshotStore.publishNoActive(accountKey, forceWidgetUpdate)
        }
    }
}

private data class WidgetSourceState(
    val accountKey: String?,
    val challengeState: ChallengeState,
    val records: Collection<ExpenseRecord>,
    val restState: RestState
)

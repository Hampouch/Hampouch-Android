package com.example.hampouch.ui.widget

import android.util.Log
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.RestState
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.domain.repository.RestRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "HomeWidgetPublisher"

/** 앱 상태 변경을 감지해 주기적인 네트워크 조회 없이 위젯 스냅샷을 갱신한다. */
@Singleton
class HomeWidgetStatePublisher @Inject constructor(
    private val authRepository: AuthRepository,
    private val challengeRepository: ChallengeRepository,
    private val expenseRepository: ExpenseRepository,
    private val restRepository: RestRepository,
    private val snapshotStore: HomeWidgetSnapshotStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val publishMutex = Mutex()
    private var started = false
    private var observedAccount: String? = null
    private var hadActiveChallenge = false

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
            }.collect { source ->
                val error = runCatching { publish(source) }.exceptionOrNull() ?: return@collect
                if (error is CancellationException) throw error
                Log.e(TAG, "위젯 상태 갱신에 실패했습니다.", error)
            }
        }
    }

    suspend fun publishSessionStarted() {
        publishMutex.withLock {
            val accountKey = authRepository.userSession.first()?.userId?.toString()
            if (accountKey == null) {
                publishLoggedOutLocked()
                return@withLock
            }
            snapshotStore.markSession(accountKey)
            observedAccount = accountKey
            hadActiveChallenge = false
        }
    }

    suspend fun publishAfterHomeSync() {
        val session = authRepository.userSession.first()
        publish(
            source = WidgetSourceState(
                accountKey = session?.userId?.toString(),
                challengeState = challengeRepository.state.value,
                records = expenseRepository.records.value.values,
                restState = restRepository.restState.value
            ),
            noActiveConfirmed = true
        )
    }

    suspend fun publishLoggedOut() {
        publishMutex.withLock {
            if (authRepository.userSession.first() != null) return@withLock
            publishLoggedOutLocked()
        }
    }

    private suspend fun publishLoggedOutLocked() {
        observedAccount = null
        hadActiveChallenge = false
        snapshotStore.publishLoggedOut()
    }

    private suspend fun publish(source: WidgetSourceState, noActiveConfirmed: Boolean = false) =
        publishMutex.withLock {
            val currentAccount = authRepository.userSession.first()?.userId?.toString()
            if (source.accountKey != currentAccount) return@withLock
            publishLocked(source, noActiveConfirmed)
        }

    private suspend fun publishLocked(source: WidgetSourceState, noActiveConfirmed: Boolean) {
        val accountKey = source.accountKey
        if (accountKey == null) {
            publishLoggedOutLocked()
            return
        }
        if (observedAccount != accountKey) {
            snapshotStore.markSession(accountKey)
            observedAccount = accountKey
            hadActiveChallenge = false
        }

        val resting = source.restState as? RestState.Resting
        if (resting != null) {
            snapshotStore.publishResting(accountKey, resting.plannedResumeDate)
            return
        }

        val today = LocalDate.now()
        val challenge = source.challengeState.challengeFor(today)
        if (challenge != null) {
            hadActiveChallenge = true
            val todaySpent = source.records.filter { it.date == today }.sumOf { it.amount }
            snapshotStore.publishChallenge(accountKey, challenge, todaySpent)
        } else if (hadActiveChallenge || noActiveConfirmed) {
            hadActiveChallenge = false
            snapshotStore.publishNoActive(accountKey)
        }
    }
}

private data class WidgetSourceState(
    val accountKey: String?,
    val challengeState: ChallengeState,
    val records: Collection<ExpenseRecord>,
    val restState: RestState
)

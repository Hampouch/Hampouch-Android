package com.example.hampouch.ui.widget

import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.RestState
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.domain.repository.RestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

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
            }.collect(::publish)
        }
    }

    /** 서버 동기화가 성공했고 진행 중 챌린지가 없다는 사실이 확정된 경우 호출한다. */
    fun publishNoActiveAfterSync() {
        scope.launch {
            val accountKey = authRepository.userSession.first()?.userId?.toString() ?: return@launch
            if (restRepository.restState.value is RestState.Resting) return@launch
            if (challengeRepository.state.value.challengeFor(LocalDate.now()) != null) return@launch
            hadActiveChallenge = false
            snapshotStore.publishNoActive(accountKey)
        }
    }

    private suspend fun publish(source: WidgetSourceState) {
        val accountKey = source.accountKey
        if (accountKey == null) {
            observedAccount = null
            hadActiveChallenge = false
            snapshotStore.publishLoggedOut()
            return
        }
        if (observedAccount != accountKey) {
            observedAccount = accountKey
            hadActiveChallenge = false
            snapshotStore.markSession(accountKey)
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
        } else if (hadActiveChallenge) {
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

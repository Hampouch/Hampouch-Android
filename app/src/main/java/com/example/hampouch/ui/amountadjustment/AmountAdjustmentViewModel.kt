package com.example.hampouch.ui.amountadjustment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.data.repository.PendingChallengeResultStore
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.PendingChallengeResult
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.model.recommendedTightenedTarget
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.ui.challengeresult.ChallengeResultMockData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface AmountAdjustmentEvent {
    data class ChallengeAbandoned(val challengeId: String, val suggestedTargetAmount: Int) : AmountAdjustmentEvent

    data object GoalAmountUpdated : AmountAdjustmentEvent

    data class ShowMessage(val message: String) : AmountAdjustmentEvent
}

internal fun ChallengeState.challengeForAbandonResult(challengeId: String) =
    challengeById(challengeId)

@HiltViewModel
class AmountAdjustmentViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val expenseRepository: ExpenseRepository,
    private val authRepository: AuthRepository,
    private val pendingChallengeResultStore: PendingChallengeResultStore
) : ViewModel() {

    val challengeState: StateFlow<ChallengeState> = challengeRepository.state

    private val _events = Channel<AmountAdjustmentEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun recordsForDate(date: LocalDate): List<ExpenseRecord> = expenseRepository.recordsForDate(date)

    fun spentOnDate(date: LocalDate): Int = recordsForDate(date).sumOf { it.amount }

    fun hasRecordOnDate(date: LocalDate): Boolean =
        recordsForDate(date).isNotEmpty() || date in expenseRepository.daysWithRecord.value

    fun updateTargetAmount(newTargetAmount: Int) {
        viewModelScope.launch {
            challengeRepository.updateTargetAmount(newTargetAmount)
                .onSuccess { _events.send(AmountAdjustmentEvent.GoalAmountUpdated) }
                .onFailure { error ->
                    _events.send(
                        AmountAdjustmentEvent.ShowMessage(error.toUserMessage("목표 금액 조정에 실패했습니다."))
                    )
                }
        }
    }

    fun abandonChallenge(challengeId: String) {
        viewModelScope.launch {
            challengeRepository.abandonChallenge()
                .onSuccess {
                    val state = challengeRepository.state.value
                    val abandoned = state.challengeForAbandonResult(challengeId) ?: return@onSuccess
                    val result = ChallengeResultMockData.forChallenge(
                        abandoned,
                        state,
                        ::recordsForDate,
                        ::hasRecordOnDate
                    )
                    val suggested = recommendedTightenedTarget(result.actualAmount)
                    authRepository.userSession.first()?.let { session ->
                        pendingChallengeResultStore.save(
                            PendingChallengeResult(
                                userId = session.userId,
                                challengeId = challengeId,
                                title = result.title,
                                periodStart = result.periodStart,
                                periodEnd = result.periodEnd,
                                totalDays = result.totalDays,
                                successDays = result.successDays,
                                streakDays = result.streakDays,
                                amountValue = result.amountValue,
                                goalAmount = result.goalAmount,
                                actualAmount = result.actualAmount,
                                dailyLimit = result.dailyLimit,
                                emotionStats = result.emotionStats,
                                dailyRecords = result.dailyRecords
                            )
                        )
                    }
                    _events.send(AmountAdjustmentEvent.ChallengeAbandoned(challengeId, suggested))
                }
                .onFailure { error ->
                    _events.send(
                        AmountAdjustmentEvent.ShowMessage(error.toUserMessage("중도 포기 처리에 실패했습니다."))
                    )
                }
        }
    }

}

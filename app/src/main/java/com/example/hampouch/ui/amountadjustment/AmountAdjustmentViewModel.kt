package com.example.hampouch.ui.amountadjustment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.model.recommendedTightenedTarget
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.ui.challengeresult.ChallengeResultMockData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface AmountAdjustmentEvent {
    data class ChallengeAbandoned(val challengeId: String, val suggestedTargetAmount: Int) : AmountAdjustmentEvent

    data object GoalAmountUpdated : AmountAdjustmentEvent

    data class ShowMessage(val message: String) : AmountAdjustmentEvent
}

@HiltViewModel
class AmountAdjustmentViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    val challengeState: StateFlow<ChallengeState> = challengeRepository.state

    private val _events = Channel<AmountAdjustmentEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun recordsForDate(date: LocalDate): List<ExpenseRecord> = expenseRepository.recordsForDate(date)

    fun spentOnDate(date: LocalDate): Int = recordsForDate(date).sumOf { it.amount }

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
                    val suggested = state.activeChallenge?.let { active ->
                        val actualAmount = ChallengeResultMockData
                            .forChallenge(active, state, ::recordsForDate)
                            .actualAmount
                        recommendedTightenedTarget(actualAmount)
                    } ?: 0
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

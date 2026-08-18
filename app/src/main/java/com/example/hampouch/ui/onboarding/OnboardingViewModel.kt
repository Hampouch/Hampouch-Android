package com.example.hampouch.ui.onboarding

import androidx.lifecycle.ViewModel
import com.example.hampouch.data.repository.OnboardingLocalStore
import com.example.hampouch.domain.model.ChallengePeriod
import com.example.hampouch.domain.model.OnboardingRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OnboardingFlowUiState(
    val step: OnboardingStep = OnboardingStep.SPLASH,
    val draft: OnboardingUiState = OnboardingUiState(),
    val showSkipConfirmDialog: Boolean = false,
    val showExistingLoginConfirmDialog: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    onboardingLocalStore: OnboardingLocalStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        OnboardingFlowUiState(
            step = if (onboardingLocalStore.consumeSkipNextSplash()) {
                OnboardingStep.EXPENSE_DIAGNOSIS
            } else {
                OnboardingStep.SPLASH
            }
        )
    )
    val uiState: StateFlow<OnboardingFlowUiState> = _uiState.asStateFlow()

    fun finishSplash() = updateStep(OnboardingStep.EXPENSE_DIAGNOSIS)
    fun changeExpense(value: Int?) = updateDraft { copy(lastMonthFoodExpense = value) }
    fun changePeriodEnabled(enabled: Boolean) = updateDraft {
        copy(periodEnabled = enabled, dateFixed = if (enabled) false else dateFixed)
    }
    fun changePeriod(days: Int?) = updateDraft { copy(challengePeriodDays = days) }
    fun changeDateFixed(enabled: Boolean) = updateDraft {
        copy(dateFixed = enabled, periodEnabled = if (enabled) false else periodEnabled)
    }
    fun changeStartDate(date: LocalDate?) = updateDraft { copy(startDate = date) }
    fun changeTotalTarget(value: Int?) = updateDraft { copy(totalTargetAmount = value) }

    fun next() = updateStep(
        when (_uiState.value.step) {
            OnboardingStep.SPLASH -> OnboardingStep.EXPENSE_DIAGNOSIS
            OnboardingStep.EXPENSE_DIAGNOSIS -> OnboardingStep.PERIOD_SETTING
            OnboardingStep.PERIOD_SETTING -> OnboardingStep.GOAL_SETTING
            OnboardingStep.GOAL_SETTING -> OnboardingStep.GOAL_SETTING
        }
    )

    fun back() = updateStep(
        when (_uiState.value.step) {
            OnboardingStep.PERIOD_SETTING -> OnboardingStep.EXPENSE_DIAGNOSIS
            OnboardingStep.GOAL_SETTING -> OnboardingStep.PERIOD_SETTING
            else -> _uiState.value.step
        }
    )

    fun showSkipConfirmation() {
        _uiState.value = _uiState.value.copy(showSkipConfirmDialog = true)
    }

    fun dismissSkipConfirmation() {
        _uiState.value = _uiState.value.copy(showSkipConfirmDialog = false)
    }

    fun showExistingLoginConfirmation() {
        _uiState.value = _uiState.value.copy(showExistingLoginConfirmDialog = true)
    }

    fun dismissExistingLoginConfirmation() {
        _uiState.value = _uiState.value.copy(showExistingLoginConfirmDialog = false)
    }

    fun buildRequest(): OnboardingRequest? {
        val draft = _uiState.value.draft
        val period = if (draft.dateFixed) {
            draft.startDate?.let(ChallengePeriod::FixedStart) ?: return null
        } else {
            draft.challengePeriodDays?.takeIf { it > 0 }?.let(ChallengePeriod::Duration) ?: return null
        }
        val days = OnboardingCalculations.impliedPeriodDays(draft) ?: return null
        val recommended = OnboardingCalculations.recommendedTotalTarget(draft.lastMonthFoodExpense, days)
        val total = (draft.totalTargetAmount ?: recommended)?.takeIf { it > 0 } ?: return null
        return OnboardingRequest(
            lastMonthFoodExpense = draft.lastMonthFoodExpense,
            period = period,
            dailyTargetAmount = (total.toDouble() / days).roundToInt(),
            totalTargetAmount = total
        )
    }

    private fun updateStep(step: OnboardingStep) {
        _uiState.value = _uiState.value.copy(step = step)
    }

    private fun updateDraft(transform: OnboardingUiState.() -> OnboardingUiState) {
        _uiState.value = _uiState.value.copy(draft = _uiState.value.draft.transform())
    }
}

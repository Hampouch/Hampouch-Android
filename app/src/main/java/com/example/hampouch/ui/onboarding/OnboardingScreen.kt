package com.example.hampouch.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.hampouch.data.model.ChallengePeriodType
import com.example.hampouch.data.model.OnboardingRequest
import com.example.hampouch.ui.onboarding.steps.CategorySelectStep
import com.example.hampouch.ui.onboarding.steps.ChallengeGoalStep
import com.example.hampouch.ui.onboarding.steps.ExpenseDiagnosisStep
import com.example.hampouch.ui.onboarding.steps.PeriodStep
import com.example.hampouch.ui.onboarding.steps.SplashStep
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun OnboardingRoute(
    onOnboardingComplete: (OnboardingRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(OnboardingStep.SPLASH) }
    var uiState by remember { mutableStateOf(OnboardingUiState()) }

    AnimatedContent(
        targetState = step,
        modifier = modifier,
        transitionSpec = { fadeIn().togetherWith(fadeOut()) },
        label = "onboarding_step_transition"
    ) { currentStep ->
        when (currentStep) {
            OnboardingStep.SPLASH -> SplashStep(
                onTimeout = { step = OnboardingStep.EXPENSE_DIAGNOSIS }
            )

            OnboardingStep.EXPENSE_DIAGNOSIS -> ExpenseDiagnosisStep(
                state = uiState,
                onExpenseChange = { uiState = uiState.copy(lastMonthFoodExpense = it) },
                onNext = { step = OnboardingStep.PERIOD_SETTING },
                onBack = {}
            )

            OnboardingStep.PERIOD_SETTING -> PeriodStep(
                state = uiState,
                onPeriodChange = { uiState = uiState.copy(challengePeriodDays = it) },
                onDateFixedChange = { uiState = uiState.copy(dateFixed = it) },
                onStartDateChange = { uiState = uiState.copy(startDate = it) },
                onNext = { step = OnboardingStep.GOAL_SETTING },
                onBack = { step = OnboardingStep.EXPENSE_DIAGNOSIS }
            )

            OnboardingStep.GOAL_SETTING -> ChallengeGoalStep(
                state = uiState,
                onDailyTargetChange = { uiState = uiState.copy(dailyTargetAmount = it) },
                onNext = { step = OnboardingStep.CATEGORY_SELECT },
                onBack = { step = OnboardingStep.PERIOD_SETTING }
            )

            OnboardingStep.CATEGORY_SELECT -> CategorySelectStep(
                selectedCategoryIds = uiState.selectedCategoryIds,
                onToggleCategory = { categoryId ->
                    uiState = uiState.copy(
                        selectedCategoryIds = if (categoryId in uiState.selectedCategoryIds) {
                            uiState.selectedCategoryIds - categoryId
                        } else {
                            uiState.selectedCategoryIds + categoryId
                        }
                    )
                },
                onStart = {
                    val periodType = when (uiState.challengePeriodDays) {
                        7 -> ChallengePeriodType.ONE_WEEK
                        14 -> ChallengePeriodType.TWO_WEEKS
                        31 -> ChallengePeriodType.ONE_MONTH
                        else -> ChallengePeriodType.CUSTOM
                    }
                    onOnboardingComplete(
                        OnboardingRequest(
                            lastMonthFoodExpense = uiState.lastMonthFoodExpense,
                            challengePeriodType = periodType,
                            customPeriodDays = uiState.challengePeriodDays,
                            dateFixed = uiState.dateFixed,
                            startDate = uiState.startDate,
                            dailyTargetAmount = uiState.dailyTargetAmount,
                            totalTargetAmount = uiState.dailyTargetAmount?.let { daily ->
                                uiState.challengePeriodDays?.let { period -> period * daily }
                            },
                            topSpendingCategoryIds = uiState.selectedCategoryIds.toList()
                        )
                    )
                },
                onBack = { step = OnboardingStep.GOAL_SETTING }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingRoutePreview() {
    HampouchTheme {
        OnboardingRoute(onOnboardingComplete = {})
    }
}

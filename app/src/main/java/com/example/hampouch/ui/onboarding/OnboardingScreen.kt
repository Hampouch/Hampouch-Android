package com.example.hampouch.ui.onboarding

import androidx.activity.compose.BackHandler
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
import com.example.hampouch.ui.dialog.OnboardingSkipConfirmDialog
import com.example.hampouch.ui.onboarding.steps.CategorySelectStep
import com.example.hampouch.ui.onboarding.steps.ChallengeGoalStep
import com.example.hampouch.ui.onboarding.steps.ExpenseDiagnosisStep
import com.example.hampouch.ui.onboarding.steps.PeriodStep
import com.example.hampouch.ui.onboarding.steps.SplashStep
import com.example.hampouch.ui.theme.HampouchTheme
import kotlin.math.roundToInt

private fun buildOnboardingRequest(uiState: OnboardingUiState): OnboardingRequest {
    val periodType = when (uiState.challengePeriodDays) {
        7 -> ChallengePeriodType.ONE_WEEK
        14 -> ChallengePeriodType.TWO_WEEKS
        30 -> ChallengePeriodType.ONE_MONTH
        else -> ChallengePeriodType.CUSTOM
    }
    val impliedPeriodDays = OnboardingCalculations.impliedPeriodDays(uiState)
    val recommendedTotalTarget =
        OnboardingCalculations.recommendedTotalTarget(uiState.lastMonthFoodExpense, impliedPeriodDays)
    val totalTarget = uiState.totalTargetAmount ?: recommendedTotalTarget
    val dailyTarget = impliedPeriodDays?.takeIf { it > 0 }?.let { period ->
        totalTarget?.let { total -> (total.toDouble() / period).roundToInt() }
    }
    return OnboardingRequest(
        lastMonthFoodExpense = uiState.lastMonthFoodExpense,
        challengePeriodType = periodType,
        customPeriodDays = uiState.challengePeriodDays,
        dateFixed = uiState.dateFixed,
        startDate = uiState.startDate,
        dailyTargetAmount = dailyTarget,
        totalTargetAmount = totalTarget,
        topSpendingCategoryIds = uiState.selectedCategoryIds.toList()
    )
}

@Composable
fun OnboardingRoute(
    onOnboardingComplete: (OnboardingRequest) -> Unit,
    onNavigateToLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(OnboardingStep.SPLASH) }
    var uiState by remember { mutableStateOf(OnboardingUiState()) }
    var showSkipConfirmDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = step != OnboardingStep.SPLASH) {
        when (step) {
            OnboardingStep.PERIOD_SETTING -> step = OnboardingStep.EXPENSE_DIAGNOSIS
            OnboardingStep.GOAL_SETTING -> step = OnboardingStep.PERIOD_SETTING
            OnboardingStep.CATEGORY_SELECT -> step = OnboardingStep.GOAL_SETTING
            else -> Unit
        }
    }

    if (showSkipConfirmDialog) {
        OnboardingSkipConfirmDialog(
            onCancel = { showSkipConfirmDialog = false },
            onConfirm = {
                showSkipConfirmDialog = false
                onOnboardingComplete(buildOnboardingRequest(uiState))
            }
        )
    }

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
                onBack = {},
                onSkipClick = { showSkipConfirmDialog = true },
                onNavigateToLogin = { showSkipConfirmDialog = true }
            )

            OnboardingStep.PERIOD_SETTING -> PeriodStep(
                state = uiState,
                onPeriodEnabledChange = { enabled ->
                    uiState = uiState.copy(
                        periodEnabled = enabled,
                        dateFixed = if (enabled) false else uiState.dateFixed
                    )
                },
                onPeriodChange = { uiState = uiState.copy(challengePeriodDays = it) },
                onDateFixedChange = { enabled ->
                    uiState = uiState.copy(
                        dateFixed = enabled,
                        periodEnabled = if (enabled) false else uiState.periodEnabled
                    )
                },
                onStartDateChange = { uiState = uiState.copy(startDate = it) },
                onNext = { step = OnboardingStep.GOAL_SETTING },
                onBack = { step = OnboardingStep.EXPENSE_DIAGNOSIS },
                onNavigateToLogin = { showSkipConfirmDialog = true }
            )

            OnboardingStep.GOAL_SETTING -> ChallengeGoalStep(
                state = uiState,
                onTotalTargetChange = { uiState = uiState.copy(totalTargetAmount = it) },
                onNext = { step = OnboardingStep.CATEGORY_SELECT },
                onBack = { step = OnboardingStep.PERIOD_SETTING },
                onNavigateToLogin = { showSkipConfirmDialog = true }
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
                onStart = { onOnboardingComplete(buildOnboardingRequest(uiState)) },
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

package com.example.hampouch.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.hampouch.domain.model.OnboardingRequest
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hampouch.ui.dialog.OnboardingSkipConfirmDialog
import com.example.hampouch.ui.onboarding.steps.CategorySelectStep
import com.example.hampouch.ui.onboarding.steps.ChallengeGoalStep
import com.example.hampouch.ui.onboarding.steps.ExpenseDiagnosisStep
import com.example.hampouch.ui.onboarding.steps.PeriodStep
import com.example.hampouch.ui.onboarding.steps.SplashStep
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun OnboardingRoute(
    onOnboardingComplete: (OnboardingRequest) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val flowState by viewModel.uiState.collectAsStateWithLifecycle()
    OnboardingScreen(
        flowState = flowState,
        onNavigateToLogin = onNavigateToLogin,
        onFinishSplash = viewModel::finishSplash,
        onExpenseChange = viewModel::changeExpense,
        onPeriodEnabledChange = viewModel::changePeriodEnabled,
        onPeriodChange = viewModel::changePeriod,
        onDateFixedChange = viewModel::changeDateFixed,
        onStartDateChange = viewModel::changeStartDate,
        onTotalTargetChange = viewModel::changeTotalTarget,
        onToggleCategory = viewModel::toggleCategory,
        onNext = viewModel::next,
        onBack = viewModel::back,
        onShowSkip = viewModel::showSkipConfirmation,
        onDismissSkip = viewModel::dismissSkipConfirmation,
        onSubmit = { viewModel.buildRequest()?.let(onOnboardingComplete) },
        modifier = modifier
    )
}

@Composable
fun OnboardingScreen(
    flowState: OnboardingFlowUiState,
    onNavigateToLogin: () -> Unit,
    onFinishSplash: () -> Unit,
    onExpenseChange: (Int) -> Unit,
    onPeriodEnabledChange: (Boolean) -> Unit,
    onPeriodChange: (Int) -> Unit,
    onDateFixedChange: (Boolean) -> Unit,
    onStartDateChange: (java.time.LocalDate) -> Unit,
    onTotalTargetChange: (Int) -> Unit,
    onToggleCategory: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onShowSkip: () -> Unit,
    onDismissSkip: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val step = flowState.step
    val uiState = flowState.draft

    BackHandler(enabled = step != OnboardingStep.SPLASH && step != OnboardingStep.EXPENSE_DIAGNOSIS) {
        when (step) {
            OnboardingStep.PERIOD_SETTING, OnboardingStep.GOAL_SETTING,
            OnboardingStep.CATEGORY_SELECT -> onBack()
            else -> Unit
        }
    }

    if (flowState.showSkipConfirmDialog) {
        OnboardingSkipConfirmDialog(
            onCancel = onDismissSkip,
            onConfirm = {
                onDismissSkip()
                onNavigateToLogin()
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
                onTimeout = onFinishSplash
            )

            OnboardingStep.EXPENSE_DIAGNOSIS -> ExpenseDiagnosisStep(
                state = uiState,
                onExpenseChange = onExpenseChange,
                onNext = onNext,
                onBack = onNavigateToLogin,
                onSkipClick = onShowSkip,
                onNavigateToLogin = onShowSkip,
                onExistingMemberLogin = onNavigateToLogin
            )

            OnboardingStep.PERIOD_SETTING -> PeriodStep(
                state = uiState,
                onPeriodEnabledChange = onPeriodEnabledChange,
                onPeriodChange = onPeriodChange,
                onDateFixedChange = onDateFixedChange,
                onStartDateChange = onStartDateChange,
                onNext = onNext,
                onBack = onBack,
                onNavigateToLogin = onShowSkip
            )

            OnboardingStep.GOAL_SETTING -> ChallengeGoalStep(
                state = uiState,
                onTotalTargetChange = onTotalTargetChange,
                onNext = onNext,
                onBack = onBack,
                onNavigateToLogin = onShowSkip
            )

            OnboardingStep.CATEGORY_SELECT -> CategorySelectStep(
                selectedCategoryIds = uiState.selectedCategoryIds,
                onToggleCategory = onToggleCategory,
                onStart = onSubmit,
                onBack = onBack
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingRoutePreview() {
    HampouchTheme {
        OnboardingScreen(
            flowState = OnboardingFlowUiState(),
            onNavigateToLogin = {}, onFinishSplash = {},
            onExpenseChange = {}, onPeriodEnabledChange = {}, onPeriodChange = {},
            onDateFixedChange = {}, onStartDateChange = {}, onTotalTargetChange = {},
            onToggleCategory = {}, onNext = {}, onBack = {}, onShowSkip = {}, onDismissSkip = {},
            onSubmit = {}
        )
    }
}

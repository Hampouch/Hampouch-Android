package com.example.hampouch.ui.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.onboarding.OnboardingUiState
import com.example.hampouch.ui.onboarding.components.EditableAmountRow
import com.example.hampouch.ui.onboarding.components.OnboardingBulletList
import com.example.hampouch.ui.onboarding.components.OnboardingCaptionText
import com.example.hampouch.ui.onboarding.components.OnboardingHeaderCard
import com.example.hampouch.ui.onboarding.components.OnboardingPrimaryButton
import com.example.hampouch.ui.onboarding.components.OnboardingProgressBar
import com.example.hampouch.ui.onboarding.components.OnboardingSecondaryButton
import com.example.hampouch.ui.onboarding.components.OnboardingTopBar
import com.example.hampouch.ui.onboarding.components.SectionCard
import com.example.hampouch.ui.onboarding.components.SkipText
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun ExpenseDiagnosisStep(
    state: OnboardingUiState,
    onExpenseChange: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkipClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wonSuffix = stringResource(R.string.onboarding_won_suffix)

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OnboardingTopBar(onBack = onBack)

            OnboardingProgressBar(currentStep = 1, totalSteps = 4)

            OnboardingHeaderCard(
                stepNumber = 1,
                stepLabel = stringResource(R.string.onboarding_step1_badge),
                title = stringResource(R.string.onboarding_step1_title)
            )

            OnboardingCaptionText(text = stringResource(R.string.onboarding_step1_caption))

            SectionCard {
                EditableAmountRow(
                    label = stringResource(R.string.onboarding_last_month_expense_label),
                    value = state.lastMonthFoodExpense,
                    onValueChange = onExpenseChange,
                    placeholder = stringResource(R.string.onboarding_direct_input),
                    suffix = wonSuffix
                )
            }

            OnboardingBulletList(
                lines = listOf(
                    stringResource(R.string.onboarding_step1_bullet1),
                    stringResource(R.string.onboarding_step1_bullet2)
                )
            )

            Box(modifier = Modifier.weight(1f))

            SkipText(text = stringResource(R.string.onboarding_skip), onClick = onSkipClick)

            OnboardingSecondaryButton(
                text = stringResource(R.string.onboarding_existing_login),
                onClick = onNavigateToLogin
            )

            OnboardingPrimaryButton(
                text = stringResource(R.string.onboarding_button_next),
                enabled = state.lastMonthFoodExpense != null,
                onClick = onNext
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseDiagnosisStepPreview() {
    HampouchTheme {
        ExpenseDiagnosisStep(
            state = OnboardingUiState(lastMonthFoodExpense = 452000),
            onExpenseChange = {},
            onNext = {},
            onBack = {},
            onSkipClick = {},
            onNavigateToLogin = {}
        )
    }
}

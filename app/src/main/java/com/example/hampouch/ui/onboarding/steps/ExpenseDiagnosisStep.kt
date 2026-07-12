package com.example.hampouch.ui.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.onboarding.OnboardingUiState
import com.example.hampouch.ui.onboarding.components.DirectInputOverlay
import com.example.hampouch.ui.onboarding.components.LabeledInputRow
import com.example.hampouch.ui.onboarding.components.OnboardingBottomNavBar
import com.example.hampouch.ui.onboarding.components.OnboardingCaptionText
import com.example.hampouch.ui.onboarding.components.OnboardingFootnoteText
import com.example.hampouch.ui.onboarding.components.OnboardingHeaderCard
import com.example.hampouch.ui.onboarding.components.OnboardingPrimaryButton
import com.example.hampouch.ui.onboarding.components.OnboardingProgressBar
import com.example.hampouch.ui.onboarding.components.OnboardingTopBar
import com.example.hampouch.ui.onboarding.components.SectionCard
import com.example.hampouch.ui.onboarding.components.SkipText
import com.example.hampouch.ui.onboarding.components.toWonText
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun ExpenseDiagnosisStep(
    state: OnboardingUiState,
    onExpenseChange: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sheetVisible by remember { mutableStateOf(false) }
    var amountText by remember(state.lastMonthFoodExpense) {
        mutableStateOf(state.lastMonthFoodExpense?.toString().orEmpty())
    }
    val wonSuffix = stringResource(R.string.onboarding_won_suffix)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = { OnboardingBottomNavBar() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OnboardingTopBar(onBack = onBack)

            OnboardingProgressBar(currentStep = 1, totalSteps = 3)

            OnboardingHeaderCard(
                stepNumber = 1,
                stepLabel = stringResource(R.string.onboarding_step1_badge),
                title = stringResource(R.string.onboarding_step2_title)
            )

            OnboardingCaptionText(text = stringResource(R.string.onboarding_step2_caption))

            SectionCard {
                LabeledInputRow(
                    label = stringResource(R.string.onboarding_last_month_expense_label),
                    valueText = state.lastMonthFoodExpense?.let { "${it.toWonText()}$wonSuffix" }
                        ?: stringResource(R.string.onboarding_direct_input),
                    onClick = { sheetVisible = true }
                )
            }

            OnboardingFootnoteText(text = stringResource(R.string.onboarding_step2_footnote))

            Box(modifier = Modifier.weight(1f))

            SkipText(text = stringResource(R.string.onboarding_skip), onClick = onNext)

            OnboardingPrimaryButton(
                text = stringResource(R.string.onboarding_button_next),
                enabled = state.lastMonthFoodExpense != null,
                onClick = onNext
            )
        }
    }

    if (sheetVisible) {
        DirectInputOverlay(
            valueText = amountText,
            onValueChange = { newValue ->
                amountText = newValue
                newValue.toIntOrNull()?.let(onExpenseChange)
            },
            placeholder = stringResource(R.string.onboarding_step3_field_placeholder),
            suffix = wonSuffix,
            onDismiss = { sheetVisible = false }
        )
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
            onBack = {}
        )
    }
}

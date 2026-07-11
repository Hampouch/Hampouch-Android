package com.example.hampouch.ui.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.onboarding.OnboardingUiState
import com.example.hampouch.ui.onboarding.components.AmountInputSheet
import com.example.hampouch.ui.onboarding.components.FloatingNextButton
import com.example.hampouch.ui.onboarding.components.LabeledInputRow
import com.example.hampouch.ui.onboarding.components.OnboardingHeaderCard
import com.example.hampouch.ui.onboarding.components.OnboardingPrimaryButton
import com.example.hampouch.ui.onboarding.components.SectionCard
import com.example.hampouch.ui.onboarding.components.toWonText
import com.example.hampouch.ui.theme.HampouchTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDiagnosisStep(
    state: OnboardingUiState,
    onExpenseChange: (Int) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sheetVisible by remember { mutableStateOf(false) }
    var amountText by remember(state.lastMonthFoodExpense) {
        mutableStateOf(state.lastMonthFoodExpense?.toString().orEmpty())
    }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val wonSuffix = stringResource(R.string.onboarding_won_suffix)

    fun closeSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { sheetVisible = false }
    }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OnboardingHeaderCard(
                title = stringResource(R.string.onboarding_step2_title),
                subtitle = stringResource(R.string.onboarding_step2_subtitle)
            )

            SectionCard {
                LabeledInputRow(
                    label = stringResource(R.string.onboarding_last_month_expense_label),
                    valueText = state.lastMonthFoodExpense?.let { "${it.toWonText()}$wonSuffix" }
                        ?: stringResource(R.string.onboarding_direct_input),
                    onClick = { sheetVisible = true }
                )
            }

            Box(modifier = Modifier.weight(1f))

            OnboardingPrimaryButton(
                text = stringResource(R.string.onboarding_next),
                enabled = state.lastMonthFoodExpense != null,
                onClick = onNext
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                FloatingNextButton(onClick = onNext)
            }
        }
    }

    if (sheetVisible) {
        AmountInputSheet(
            title = stringResource(R.string.onboarding_step3_title),
            amountText = amountText,
            onAmountTextChange = { amountText = it },
            onConfirm = {
                amountText.toIntOrNull()?.let(onExpenseChange)
                closeSheet()
            },
            onDismiss = { closeSheet() },
            sheetState = sheetState
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
            onNext = {}
        )
    }
}

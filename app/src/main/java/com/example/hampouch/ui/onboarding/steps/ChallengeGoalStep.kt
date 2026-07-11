package com.example.hampouch.ui.onboarding.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.hampouch.ui.onboarding.OnboardingMockData
import com.example.hampouch.ui.onboarding.OnboardingUiState
import com.example.hampouch.ui.onboarding.components.AmountInputSheet
import com.example.hampouch.ui.onboarding.components.FloatingNextButton
import com.example.hampouch.ui.onboarding.components.LabeledInputRow
import com.example.hampouch.ui.onboarding.components.OnboardingHeaderCard
import com.example.hampouch.ui.onboarding.components.OnboardingPrimaryButton
import com.example.hampouch.ui.onboarding.components.OnboardingProgressBar
import com.example.hampouch.ui.onboarding.components.SectionCard
import com.example.hampouch.ui.onboarding.components.SegmentedSelector
import com.example.hampouch.ui.onboarding.components.toWonText
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import kotlinx.coroutines.launch

private enum class GoalSheet { NONE, SALARY_DAY, SALARY_CALENDAR, TARGET_AMOUNT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeGoalStep(
    state: OnboardingUiState,
    onPeriodChange: (Int) -> Unit,
    onSalaryDayChange: (Int) -> Unit,
    onTargetAmountChange: (Int) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSheet by remember { mutableStateOf(GoalSheet.NONE) }
    var targetAmountText by remember(state.targetAmount) {
        mutableStateOf(state.targetAmount?.toString().orEmpty())
    }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val wonSuffix = stringResource(R.string.onboarding_won_suffix)

    fun closeSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { activeSheet = GoalSheet.NONE }
    }

    val progressFilled = 1 +
        (if (state.salaryDay != null) 1 else 0) +
        (if (state.targetAmount != null) 1 else 0)

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OnboardingProgressBar(currentStep = progressFilled, totalSteps = 3)

            OnboardingHeaderCard(
                title = stringResource(R.string.onboarding_step4_title),
                subtitle = stringResource(R.string.onboarding_step4_subtitle)
            )

            SectionCard {
                Text(
                    text = stringResource(R.string.onboarding_period_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = HPSub1
                )
                SegmentedSelector(
                    options = OnboardingMockData.periodPresets.map { stringResource(it.labelResId) },
                    selectedIndex = OnboardingMockData.periodPresets.indexOfFirst { it.days == state.challengePeriodDays }.coerceAtLeast(0),
                    onSelect = { index -> onPeriodChange(OnboardingMockData.periodPresets[index].days) },
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            SectionCard {
                LabeledInputRow(
                    label = stringResource(R.string.onboarding_salary_day_label),
                    valueText = state.salaryDay?.let { stringResource(R.string.onboarding_salary_day_value, it) }
                        ?: stringResource(R.string.onboarding_direct_input),
                    onClick = { activeSheet = GoalSheet.SALARY_DAY }
                )
            }

            SectionCard {
                LabeledInputRow(
                    label = stringResource(R.string.onboarding_target_amount_label),
                    valueText = state.targetAmount?.let { "${it.toWonText()}$wonSuffix" }
                        ?: stringResource(R.string.onboarding_direct_input),
                    onClick = { activeSheet = GoalSheet.TARGET_AMOUNT }
                )
            }

            val dailyBudget = state.targetAmount?.let { amount ->
                if (state.challengePeriodDays > 0) amount / state.challengePeriodDays else null
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HPWhite, RoundedCornerShape(19.5.dp))
                    .border(1.dp, HPGray5, RoundedCornerShape(19.5.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.onboarding_daily_budget_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = HPSub1
                )
                Box(
                    modifier = Modifier
                        .height(39.dp)
                        .background(HPMain, RoundedCornerShape(19.5.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dailyBudget?.let {
                            stringResource(R.string.onboarding_daily_budget_value, it.toWonText())
                        } ?: "-",
                        style = MaterialTheme.typography.labelLarge,
                        color = HPWhite
                    )
                }
            }

            OnboardingPrimaryButton(
                text = stringResource(R.string.onboarding_button_next),
                enabled = state.salaryDay != null && state.targetAmount != null,
                onClick = onNext
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                FloatingNextButton(onClick = onNext)
            }
        }
    }

    when (activeSheet) {
        GoalSheet.SALARY_DAY -> SalaryDaySheet(
            initialSelectedDay = state.salaryDay,
            onSalaryDaySelected = {
                onSalaryDayChange(it)
                closeSheet()
            },
            onOpenCalendar = { activeSheet = GoalSheet.SALARY_CALENDAR },
            onDismiss = { closeSheet() },
            sheetState = sheetState
        )

        GoalSheet.SALARY_CALENDAR -> SalaryCalendarSheet(
            initialSelectedDay = state.salaryDay,
            onDaySelected = {
                onSalaryDayChange(it)
                closeSheet()
            },
            onDismiss = { closeSheet() },
            sheetState = sheetState
        )

        GoalSheet.TARGET_AMOUNT -> AmountInputSheet(
            title = stringResource(R.string.onboarding_step6_title),
            amountText = targetAmountText,
            onAmountTextChange = { targetAmountText = it },
            onConfirm = {
                targetAmountText.toIntOrNull()?.let(onTargetAmountChange)
                closeSheet()
            },
            onDismiss = { closeSheet() },
            sheetState = sheetState
        )

        GoalSheet.NONE -> Unit
    }
}

@Preview(showBackground = true)
@Composable
private fun ChallengeGoalStepPreview() {
    HampouchTheme {
        ChallengeGoalStep(
            state = OnboardingUiState(salaryDay = 25, targetAmount = 300000),
            onPeriodChange = {},
            onSalaryDayChange = {},
            onTargetAmountChange = {},
            onNext = {}
        )
    }
}

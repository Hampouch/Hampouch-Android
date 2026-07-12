package com.example.hampouch.ui.onboarding.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.onboarding.OnboardingMockData
import com.example.hampouch.ui.onboarding.OnboardingUiState
import com.example.hampouch.ui.onboarding.components.DirectInputOverlay
import com.example.hampouch.ui.onboarding.components.LabeledInputRow
import com.example.hampouch.ui.onboarding.components.OnboardingCaptionText
import com.example.hampouch.ui.onboarding.components.OnboardingHeaderCard
import com.example.hampouch.ui.onboarding.components.OnboardingPrimaryButton
import com.example.hampouch.ui.onboarding.components.OnboardingProgressBar
import com.example.hampouch.ui.onboarding.components.OnboardingTopBar
import com.example.hampouch.ui.onboarding.components.SectionCard
import com.example.hampouch.ui.onboarding.components.SegmentedSelector
import com.example.hampouch.ui.onboarding.components.SkipText
import com.example.hampouch.ui.onboarding.components.toWonText
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

private enum class GoalOverlay { NONE, TARGET_AMOUNT, PERIOD_CUSTOM, SALARY_CALENDAR }

@Composable
fun ChallengeGoalStep(
    state: OnboardingUiState,
    onPeriodChange: (Int) -> Unit,
    onSalaryDayChange: (Int) -> Unit,
    onResetOnSalaryDayChange: (Boolean) -> Unit,
    onTargetAmountChange: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeOverlay by remember { mutableStateOf(GoalOverlay.NONE) }
    var targetAmountText by remember(state.targetAmount) {
        mutableStateOf(state.targetAmount?.toString().orEmpty())
    }
    var periodCustomText by remember { mutableStateOf("") }
    val wonSuffix = stringResource(R.string.onboarding_won_suffix)
    val daySuffix = stringResource(R.string.onboarding_day_suffix)

    val periodLabels = OnboardingMockData.periodPresets.map { stringResource(it.labelResId) } +
        stringResource(R.string.onboarding_direct_input)
    val periodSelectedIndex = OnboardingMockData.periodPresets
        .indexOfFirst { it.days == state.challengePeriodDays }
        .let { if (it == -1) periodLabels.lastIndex else it }

    val salaryDateText = state.salaryDay?.let { day ->
        stringResource(R.string.onboarding_salary_day_recurring_format, day)
    } ?: stringResource(R.string.onboarding_direct_input)

    val dailyBudget = state.targetAmount?.let { amount ->
        if (state.challengePeriodDays > 0) amount / state.challengePeriodDays else null
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OnboardingTopBar(onBack = onBack)

            OnboardingProgressBar(currentStep = 2, totalSteps = 3)

            OnboardingHeaderCard(
                stepNumber = 2,
                stepLabel = stringResource(R.string.onboarding_step2_badge),
                title = stringResource(R.string.onboarding_step4_title)
            )

            OnboardingCaptionText(text = stringResource(R.string.onboarding_step4_caption))

            SectionCard {
                Text(
                    text = stringResource(R.string.onboarding_period_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = HPSub1
                )
                SegmentedSelector(
                    options = periodLabels,
                    selectedIndex = periodSelectedIndex,
                    onSelect = { index ->
                        if (index == periodLabels.lastIndex) {
                            activeOverlay = GoalOverlay.PERIOD_CUSTOM
                        } else {
                            onPeriodChange(OnboardingMockData.periodPresets[index].days)
                        }
                    },
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            SectionCard {
                LabeledInputRow(
                    label = stringResource(R.string.onboarding_target_amount_label),
                    valueText = state.targetAmount?.let { "${it.toWonText()}$wonSuffix" }
                        ?: stringResource(R.string.onboarding_direct_input),
                    onClick = { activeOverlay = GoalOverlay.TARGET_AMOUNT }
                )
            }

            SectionCard {
                LabeledInputRow(
                    label = stringResource(R.string.onboarding_salary_day_label),
                    valueText = salaryDateText,
                    icon = Icons.Filled.CalendarToday,
                    onClick = { activeOverlay = GoalOverlay.SALARY_CALENDAR }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HPWhite, RoundedCornerShape(16.dp))
                    .border(1.dp, HPGray5, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.onboarding_salary_reset_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = HPBlack
                    )
                    Text(
                        text = stringResource(R.string.onboarding_salary_reset_description),
                        style = MaterialTheme.typography.labelSmall,
                        color = HPText
                    )
                }
                Switch(
                    checked = state.resetOnSalaryDay,
                    onCheckedChange = onResetOnSalaryDayChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = HPWhite,
                        checkedTrackColor = HPMain,
                        uncheckedThumbColor = HPWhite,
                        uncheckedTrackColor = HPGray5
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HPSub3, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.onboarding_current_daily_limit_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPText
                )
                Text(
                    text = dailyBudget?.let { "${it.toWonText()}$wonSuffix" } ?: "-",
                    style = MaterialTheme.typography.titleSmall,
                    color = HPBlack
                )
            }

            SkipText(text = stringResource(R.string.onboarding_skip_goal), onClick = onNext)

            OnboardingPrimaryButton(
                text = stringResource(R.string.onboarding_button_next),
                enabled = state.salaryDay != null && state.targetAmount != null,
                onClick = onNext
            )
        }
    }

    when (activeOverlay) {
        GoalOverlay.TARGET_AMOUNT -> DirectInputOverlay(
            valueText = targetAmountText,
            onValueChange = { newValue ->
                targetAmountText = newValue
                newValue.toIntOrNull()?.let(onTargetAmountChange)
            },
            placeholder = stringResource(R.string.onboarding_step6_field_placeholder),
            suffix = wonSuffix,
            onDismiss = { activeOverlay = GoalOverlay.NONE }
        )

        GoalOverlay.PERIOD_CUSTOM -> DirectInputOverlay(
            valueText = periodCustomText,
            onValueChange = { newValue ->
                periodCustomText = newValue
                newValue.toIntOrNull()?.let(onPeriodChange)
            },
            placeholder = stringResource(R.string.onboarding_period_custom_placeholder),
            suffix = daySuffix,
            showMascot = false,
            onDismiss = { activeOverlay = GoalOverlay.NONE }
        )

        GoalOverlay.SALARY_CALENDAR -> SalaryCalendarSheet(
            initialSelectedDay = state.salaryDay,
            onDaySelected = {
                onSalaryDayChange(it)
                activeOverlay = GoalOverlay.NONE
            },
            onDismiss = { activeOverlay = GoalOverlay.NONE }
        )

        GoalOverlay.NONE -> Unit
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
            onResetOnSalaryDayChange = {},
            onTargetAmountChange = {},
            onNext = {},
            onBack = {}
        )
    }
}

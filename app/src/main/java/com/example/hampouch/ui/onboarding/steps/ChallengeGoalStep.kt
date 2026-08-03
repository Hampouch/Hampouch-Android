package com.example.hampouch.ui.onboarding.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.onboarding.OnboardingCalculations
import com.example.hampouch.ui.onboarding.OnboardingUiState
import com.example.hampouch.ui.onboarding.components.EditableAmountRow
import com.example.hampouch.ui.onboarding.components.OnboardingBulletList
import com.example.hampouch.ui.onboarding.components.OnboardingHeaderCard
import com.example.hampouch.ui.onboarding.components.OnboardingPrimaryButton
import com.example.hampouch.ui.onboarding.components.OnboardingProgressBar
import com.example.hampouch.ui.onboarding.components.OnboardingTopBar
import com.example.hampouch.ui.onboarding.components.SectionCard
import com.example.hampouch.ui.onboarding.components.toWonText
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import kotlin.math.roundToInt

@Composable
fun ChallengeGoalStep(
    state: OnboardingUiState,
    onTotalTargetChange: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wonSuffix = stringResource(R.string.onboarding_won_suffix)

    val impliedPeriodDays = OnboardingCalculations.impliedPeriodDays(state)
    val recommendedTotalTarget = OnboardingCalculations.recommendedTotalTarget(state.lastMonthFoodExpense, impliedPeriodDays)
    val effectiveTotalTarget = state.totalTargetAmount ?: recommendedTotalTarget
    val dailyTarget = impliedPeriodDays?.takeIf { it > 0 }?.let { period ->
        effectiveTotalTarget?.let { total -> (total.toDouble() / period).roundToInt() }
    } ?: 0

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

            OnboardingProgressBar(currentStep = 3, totalSteps = 4)

            OnboardingHeaderCard(
                stepNumber = 3,
                stepLabel = stringResource(R.string.onboarding_step3_badge),
                title = stringResource(R.string.onboarding_step3_title)
            )

            SectionCard {
                EditableAmountRow(
                    label = stringResource(R.string.onboarding_total_target_label),
                    value = effectiveTotalTarget,
                    editSeedValue = state.totalTargetAmount,
                    onValueChange = onTotalTargetChange,
                    placeholder = stringResource(R.string.onboarding_direct_input),
                    suffix = wonSuffix,
                    valueColor = HPText
                )
                OnboardingBulletList(
                    lines = listOf(stringResource(R.string.onboarding_total_target_bullet)),
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Column {
                Text(
                    text = stringResource(R.string.onboarding_daily_target_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = HPSub1
                )
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(HPMain, RoundedCornerShape(10.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "${dailyTarget.toWonText()}$wonSuffix",
                        style = MaterialTheme.typography.titleSmall,
                        color = HPWhite
                    )
                }
            }

            Box(modifier = Modifier.weight(1f))

            OnboardingPrimaryButton(
                text = stringResource(R.string.onboarding_button_next),
                enabled = true,
                onClick = onNext
            )
        }
    }
}

@Preview(showBackground = true, name = "Recommended default (기간 선택)")
@Composable
private fun ChallengeGoalStepPreview() {
    HampouchTheme {
        ChallengeGoalStep(
            state = OnboardingUiState(
                lastMonthFoodExpense = 400000,
                periodEnabled = true,
                challengePeriodDays = 30
            ),
            onTotalTargetChange = {},
            onNext = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Recommended default (날짜 고정)")
@Composable
private fun ChallengeGoalStepDateFixedPreview() {
    HampouchTheme {
        ChallengeGoalStep(
            state = OnboardingUiState(
                lastMonthFoodExpense = 400000,
                dateFixed = true
            ),
            onTotalTargetChange = {},
            onNext = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Edited target")
@Composable
private fun ChallengeGoalStepEditedPreview() {
    HampouchTheme {
        ChallengeGoalStep(
            state = OnboardingUiState(
                lastMonthFoodExpense = 400000,
                totalTargetAmount = 300000,
                periodEnabled = true,
                challengePeriodDays = 30
            ),
            onTotalTargetChange = {},
            onNext = {},
            onBack = {}
        )
    }
}

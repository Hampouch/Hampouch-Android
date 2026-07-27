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
    onDailyTargetChange: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wonSuffix = stringResource(R.string.onboarding_won_suffix)

    val computedDailyDefaultExact = state.lastMonthFoodExpense?.let { expense ->
        state.challengePeriodDays?.takeIf { it > 0 }?.let { period -> expense.toDouble() / period }
    }
    val effectiveDailyTargetExact = state.dailyTargetAmount?.toDouble() ?: computedDailyDefaultExact
    val totalTargetExact = effectiveDailyTargetExact?.let { daily ->
        (state.challengePeriodDays ?: 0) * daily
    }
    val effectiveDailyTarget = effectiveDailyTargetExact?.roundToInt()
    val totalTarget = totalTargetExact?.roundToInt()

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
                    label = stringResource(R.string.onboarding_daily_target_label),
                    value = effectiveDailyTarget,
                    onValueChange = onDailyTargetChange,
                    placeholder = stringResource(R.string.onboarding_direct_input),
                    suffix = wonSuffix,
                    valueColor = HPText
                )
                OnboardingBulletList(
                    lines = listOf(stringResource(R.string.onboarding_daily_target_bullet)),
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Column {
                Text(
                    text = stringResource(R.string.onboarding_total_target_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = HPSub1
                )
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(HPMain, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = totalTarget?.let { "${it.toWonText()}$wonSuffix" } ?: "-",
                        style = MaterialTheme.typography.titleSmall,
                        color = HPWhite
                    )
                }
            }

            Box(modifier = Modifier.weight(1f))

            OnboardingPrimaryButton(
                text = stringResource(R.string.onboarding_button_next),
                enabled = effectiveDailyTarget != null,
                onClick = {
                    if (state.dailyTargetAmount == null) {
                        computedDailyDefaultExact?.roundToInt()?.let(onDailyTargetChange)
                    }
                    onNext()
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChallengeGoalStepPreview() {
    HampouchTheme {
        ChallengeGoalStep(
            state = OnboardingUiState(
                lastMonthFoodExpense = 400000,
                challengePeriodDays = 31
            ),
            onDailyTargetChange = {},
            onNext = {},
            onBack = {}
        )
    }
}

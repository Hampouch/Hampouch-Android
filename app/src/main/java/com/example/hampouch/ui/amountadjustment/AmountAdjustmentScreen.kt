package com.example.hampouch.ui.amountadjustment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.data.model.AmountAdjustmentChallenge
import com.example.hampouch.data.repository.ChallengeRepository
import com.example.hampouch.ui.challengeresult.ChallengeResultMockData
import com.example.hampouch.ui.dialog.AbandonChallengeConfirmDialog
import com.example.hampouch.ui.dialog.AmountAdjustmentConfirmDialog
import com.example.hampouch.ui.expensedetail.DashedDivider
import com.example.hampouch.ui.expensedetail.formatWon
import com.example.hampouch.ui.onboarding.components.EditableAmountRow
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val AmountAdjustmentPeriodFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

private enum class AmountAdjustmentOption(val labelResId: Int, val multiplier: Double) {
    KEEP(R.string.amountadjustment_option_keep, 1.0),
    RELAX_10(R.string.amountadjustment_option_relax_10, 1.1),
    RELAX_20(R.string.amountadjustment_option_relax_20, 1.2)
}

private fun AmountAdjustmentOption.amountFor(targetAmount: Int): Int =
    (targetAmount * multiplier).roundToInt()

@Composable
fun AmountAdjustmentRoute(
    challenge: AmountAdjustmentChallenge,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onChallengeAbandoned: (challengeId: String, suggestedTargetAmount: Int) -> Unit = { _, _ -> }
) {
    var editCount by remember(challenge) { mutableIntStateOf(challenge.editCount) }
    var selectedOption by remember(challenge) {
        mutableStateOf<AmountAdjustmentOption?>(AmountAdjustmentOption.KEEP)
    }
    var customAmount by remember(challenge) { mutableStateOf<Int?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showAbandonConfirmDialog by remember { mutableStateOf(false) }

    val canEdit = editCount < challenge.maxEditCount && selectedOption != AmountAdjustmentOption.KEEP
    val selectedAmount = customAmount
        ?: selectedOption?.amountFor(challenge.targetAmount)
        ?: challenge.targetAmount
    val dailyFoodGoal = if (challenge.totalDays > 0) selectedAmount / challenge.totalDays else 0

    Scaffold(
        modifier = modifier.imePadding(),
        topBar = {
            AmountAdjustmentTopBar(onBackClick = onBackClick)
        },
        containerColor = HPGray2
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                stringResource(R.string.amountadjustment_headline),
                style = MaterialTheme.typography.titleMedium,
                color = HPBlack,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                stringResource(R.string.amountadjustment_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = HPText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                stringResource(R.string.amountadjustment_current_challenge_title),
                style = Body16Bold,
                color = HPBlack
            )
            Spacer(modifier = Modifier.height(10.dp))
            ChallengeInfoCard(challenge = challenge)

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                stringResource(R.string.amountadjustment_target_amount_label),
                style = Body16Bold,
                color = HPBlack
            )
            Spacer(modifier = Modifier.height(6.dp))
            OnboardingBulletNote(text = stringResource(R.string.amountadjustment_goal_amount_note))

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AmountAdjustmentOption.entries.forEach { option ->
                    AmountOptionCard(
                        label = stringResource(option.labelResId),
                        amountText = stringResource(
                            R.string.expensedetail_amount_won_format,
                            formatWon(option.amountFor(challenge.targetAmount))
                        ),
                        selected = customAmount == null && option == selectedOption,
                        onClick = {
                            selectedOption = option
                            customAmount = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            EditableAmountRow(
                label = null,
                value = customAmount,
                onValueChange = { value ->
                    customAmount = value
                    selectedOption = null
                },
                placeholder = stringResource(R.string.amountadjustment_custom_input_placeholder),
                suffix = stringResource(R.string.amountadjustment_won_suffix)
            )

            Spacer(modifier = Modifier.height(20.dp))
            DailyFoodGoalCard(amount = dailyFoodGoal)

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(
                    R.string.amountadjustment_edit_count_caption_format,
                    challenge.maxEditCount,
                    (challenge.maxEditCount - editCount).coerceAtLeast(0)
                ),
                style = MaterialTheme.typography.labelMedium,
                color = HPText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { showConfirmDialog = true },
                enabled = canEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HPMain,
                    contentColor = HPWhite,
                    disabledContainerColor = HPGray4,
                    disabledContentColor = HPText
                )
            ) {
                Text(stringResource(R.string.amountadjustment_submit_button), style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.amountadjustment_abandon_button),
                style = MaterialTheme.typography.bodyMedium,
                color = HPSub,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAbandonConfirmDialog = true }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showConfirmDialog) {
        val isLastEdit = editCount + 1 >= challenge.maxEditCount
        AmountAdjustmentConfirmDialog(
            onCancel = { showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                editCount = (editCount + 1).coerceAtMost(challenge.maxEditCount)
                ChallengeRepository.updateTargetAmount(selectedAmount)
            },
            subtext = if (isLastEdit) stringResource(R.string.amountadjustment_last_edit_warning) else null
        )
    }

    if (showAbandonConfirmDialog) {
        AbandonChallengeConfirmDialog(
            onCancel = { showAbandonConfirmDialog = false },
            onConfirm = {
                showAbandonConfirmDialog = false
                ChallengeRepository.abandonChallenge()
                val actualAmount =
                    ChallengeResultMockData.forChallenge(ChallengeRepository.activeChallenge!!).actualAmount
                val suggestedTargetAmount = ChallengeResultMockData.recommendedTightenedTarget(actualAmount)
                onChallengeAbandoned(challenge.id, suggestedTargetAmount)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AmountAdjustmentTopBar(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                stringResource(R.string.amountadjustment_top_bar_title),
                style = MaterialTheme.typography.titleSmall,
                color = HPBlack
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = HPBlack
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = HPGray2)
    )
}

@Composable
private fun ChallengeInfoCard(challenge: AmountAdjustmentChallenge, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub4)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.amountadjustment_challenge_days_format, challenge.totalDays),
                style = Body16Bold,
                color = HPBlack,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(HPMain)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    stringResource(R.string.amountadjustment_dday_format, challenge.dDay),
                    style = MaterialTheme.typography.labelLarge,
                    color = HPWhite
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(
                R.string.amountadjustment_period_format,
                challenge.periodStart.format(AmountAdjustmentPeriodFormatter),
                challenge.periodEnd.format(AmountAdjustmentPeriodFormatter)
            ),
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )
        Spacer(modifier = Modifier.height(14.dp))
        DashedDivider(color = HPSub2)
        Spacer(modifier = Modifier.height(8.dp))
        ChallengeAmountRow(
            label = stringResource(R.string.amountadjustment_target_amount_label),
            amount = challenge.targetAmount
        )
        ChallengeAmountRow(
            label = stringResource(R.string.amountadjustment_daily_limit_label),
            amount = challenge.dailyLimit
        )
        Spacer(modifier = Modifier.height(6.dp))
        DashedDivider(color = HPSub2)
        Spacer(modifier = Modifier.height(14.dp))
        val overText = buildAnnotatedString {
            append(stringResource(R.string.amountadjustment_over_amount_prefix))
            withStyle(SpanStyle(color = HPMain, fontWeight = FontWeight.Bold)) {
                append(stringResource(R.string.expensedetail_amount_won_format, formatWon(challenge.overAmount)))
            }
            append(stringResource(R.string.amountadjustment_over_amount_suffix))
        }
        Text(
            overText,
            style = MaterialTheme.typography.bodyMedium,
            color = HPBlack,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ChallengeAmountRow(label: String, amount: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = HPText)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            stringResource(R.string.expensedetail_amount_won_format, formatWon(amount)),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = HPMain
        )
    }
}

@Composable
private fun OnboardingBulletNote(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("•", style = MaterialTheme.typography.labelSmall, color = HPText)
        Text(text, style = MaterialTheme.typography.labelSmall, color = HPText)
    }
}

@Composable
private fun AmountOptionCard(
    label: String,
    amountText: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) HPMain else HPWhite)
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = if (selected) Color.Transparent else HPGray4,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) HPWhite else HPText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            amountText,
            style = Body16Bold,
            color = if (selected) HPWhite else HPBlack,
            maxLines = 1
        )
    }
}

@Composable
private fun DailyFoodGoalCard(amount: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub4)
            .padding(20.dp)
    ) {
        Text(
            stringResource(R.string.amountadjustment_daily_food_goal_title),
            style = Body16Bold,
            color = HPBlack
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(HPSub2)
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Text(
                stringResource(R.string.expensedetail_amount_won_format, formatWon(amount)),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = HPWhite,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, name = "1. 목표 금액 조정")
@Composable
private fun AmountAdjustmentScreenPreview() {
    HampouchTheme {
        AmountAdjustmentRoute(
            challenge = AmountAdjustmentMockData.challenge().copy(editCount = 0),
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "2. 목표 수정 횟수 초과")
@Composable
private fun AmountAdjustmentScreenMaxEditPreview() {
    HampouchTheme {
        AmountAdjustmentRoute(
            challenge = AmountAdjustmentMockData.challenge().copy(editCount = 1),
            onBackClick = {}
        )
    }
}

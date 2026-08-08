package com.example.hampouch.ui.nextchallenge

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.R
import com.example.hampouch.data.model.OnboardingRequest
import com.example.hampouch.data.repository.ChallengeRepository
import com.example.hampouch.ui.challengeresult.formatWon
import com.example.hampouch.ui.dialog.NextChallengeStartConfirmDialog
import com.example.hampouch.ui.home.HomeCategoryCatalog
import com.example.hampouch.ui.onboarding.components.EditableAmountRow
import com.example.hampouch.ui.onboarding.components.OnboardingBulletList
import com.example.hampouch.ui.onboarding.components.OnboardingTopBar
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.LocalDate

private const val DefaultTakeABreakTotalDays = 14

@Composable
fun NextChallengeTakeABreakRoute(
    onBackClick: () -> Unit,
    onStartChallengeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var periodEnabled by remember { mutableStateOf(false) }
    var periodDays by remember { mutableStateOf<Int?>(null) }
    var customPeriodDays by remember { mutableStateOf<Int?>(null) }
    var dateFixed by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var targetAmount by remember { mutableStateOf<Int?>(null) }
    var selectedCategoryIds by remember { mutableStateOf(setOf("delivery")) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartConfirmDialog by remember { mutableStateOf(false) }

    val explicitPeriodDays = customPeriodDays?.takeIf { it > 0 } ?: periodDays?.takeIf { it > 0 }
    val effectivePeriodDays = explicitPeriodDays ?: DefaultTakeABreakTotalDays
    val currentStartDate = startDate
    val dailyGoal = when {
        periodEnabled -> (targetAmount ?: 0) / effectivePeriodDays
        currentStartDate != null -> (targetAmount ?: 0) / monthlyTotalDays(currentStartDate)
        else -> targetAmount ?: 0
    }
    val startDateText =
        startDate?.let { "${it.year}년 ${it.monthValue}월 ${it.dayOfMonth}일" } ?: "시작일"
    val isPeriodOrDateSelected = when {
        periodEnabled -> explicitPeriodDays != null
        dateFixed -> startDate != null
        else -> false
    }
    val canStartChallenge = isPeriodOrDateSelected &&
        (targetAmount ?: 0) > 0 &&
        selectedCategoryIds.isNotEmpty()

    Scaffold(
        modifier = modifier.imePadding(),
        containerColor = HPGray2
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            OnboardingTopBar(onBack = onBackClick)

            NextChallengeHeroCard(
                title = "휴식기가 끝났어요.",
                subtitle = "다시 시작해볼까요?"
            )
            Spacer(modifier = Modifier.height(20.dp))
            Column {
                Text("챌린지 설정", style = Body16Bold, fontSize = 18.sp, color = HPBlack)
                Spacer(modifier = Modifier.height(10.dp))
                ChallengeSettingsSection(
                    periodEnabled = periodEnabled,
                    onPeriodEnabledChange = { enabled ->
                        periodEnabled = enabled
                        if (enabled) dateFixed = false
                    },
                    periodDays = periodDays,
                    onPeriodDaysChange = { days ->
                        periodDays = days
                        customPeriodDays = null
                    },
                    customPeriodDays = customPeriodDays,
                    onCustomPeriodDaysChange = { value ->
                        if (PeriodPresetDayOptions.any { it.first == value }) {
                            periodDays = value
                            customPeriodDays = null
                        } else {
                            customPeriodDays = value
                            periodDays = null
                        }
                    },
                    dateFixed = dateFixed,
                    onDateFixedChange = { enabled ->
                        dateFixed = enabled
                        if (enabled) {
                            periodEnabled = false
                        } else {
                            startDate = null
                        }
                    },
                    startDateText = startDateText,
                    onStartDateClick = { showDatePicker = true }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(HPSub4)
                    .padding(horizontal = 15.dp, vertical = 20.dp)
            ) {
                Text("챌린지 전체 식비 목표", style = Body16Bold, color = HPBlack)
                Spacer(modifier = Modifier.height(8.dp))
                EditableAmountRow(
                    label = null,
                    value = targetAmount,
                    onValueChange = { targetAmount = it },
                    placeholder = "직접 입력",
                    suffix = "원"
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("하루 식비 목표", style = Body16Bold, color = HPBlack)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(HPSub2)
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Text(
                        formatWon(dailyGoal),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = HPWhite,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(HPSub4)
                    .padding(horizontal = 15.dp, vertical = 20.dp)
            ) {
                Text("카테고리", style = Body16Bold, color = HPBlack)
                Spacer(modifier = Modifier.height(6.dp))
                OnboardingBulletList(
                    lines = listOf(
                        "중복 선택 가능",
                        "선택한 카테고리 소비 시 개입이 강해져요."
                    )
                )
                Spacer(modifier = Modifier.height(14.dp))
                HomeCategoryCatalog.categories.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { category ->
                            CategoryIconChip(
                                label = stringResource(category.labelResId),
                                iconRes = CategoryIconRes[category.id] ?: R.drawable.icon_etc,
                                selected = category.id in selectedCategoryIds,
                                onClick = {
                                    selectedCategoryIds = if (category.id in selectedCategoryIds) {
                                        selectedCategoryIds - category.id
                                    } else {
                                        selectedCategoryIds + category.id
                                    }
                                }
                            )
                        }
                        repeat(3 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = { showStartConfirmDialog = true },
                enabled = canStartChallenge,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HPMain,
                    disabledContainerColor = HPGray4,
                    disabledContentColor = HPText
                )
            ) {
                Text(
                    "챌린지 시작하기",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = HPWhite
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showDatePicker) {
        NextChallengeDatePickerDialog(
            initialDate = startDate ?: LocalDate.now(),
            onDateSelected = {
                startDate = it
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showStartConfirmDialog) {
        NextChallengeStartConfirmDialog(
            onCancel = { showStartConfirmDialog = false },
            onConfirm = {
                showStartConfirmDialog = false
                val request = OnboardingRequest(
                    dateFixed = dateFixed,
                    startDate = if (dateFixed) (startDate ?: LocalDate.now()) else null,
                    customPeriodDays = if (dateFixed) null else effectivePeriodDays,
                    totalTargetAmount = targetAmount ?: 0,
                    topSpendingCategoryIds = selectedCategoryIds.toList()
                )
                ChallengeRepository.startNewChallenge(request)
                onStartChallengeClick()
            }
        )
    }
}

@Preview(showBackground = true, name = "휴식 후 다음 챌린지")
@Composable
private fun NextChallengeTakeABreakRoutePreview() {
    HampouchTheme {
        NextChallengeTakeABreakRoute(
            onBackClick = {},
            onStartChallengeClick = {}
        )
    }
}

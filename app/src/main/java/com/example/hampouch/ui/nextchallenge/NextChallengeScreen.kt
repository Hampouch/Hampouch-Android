package com.example.hampouch.ui.nextchallenge

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.R
import com.example.hampouch.data.model.ChallengeResultStatus
import com.example.hampouch.data.model.ChallengeResultUiState
import com.example.hampouch.data.repository.ChallengeRepository
import com.example.hampouch.ui.challengeresult.ChallengeResultMockData
import com.example.hampouch.ui.challengeresult.formatWon
import com.example.hampouch.ui.dialog.NextChallengeStartConfirmDialog
import com.example.hampouch.ui.expensedetail.DashedDivider
import com.example.hampouch.ui.home.HomeCategoryCatalog
import com.example.hampouch.ui.onboarding.components.EditableAmountRow
import com.example.hampouch.ui.onboarding.components.LabeledInputRow
import com.example.hampouch.ui.onboarding.components.OnboardingBulletList
import com.example.hampouch.ui.onboarding.components.OnboardingTopBar
import com.example.hampouch.ui.onboarding.components.PeriodPresetRow
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPStatusInProgressBg
import com.example.hampouch.ui.theme.HPStatusInProgressText
import com.example.hampouch.ui.theme.HPStatusSuccessBg
import com.example.hampouch.ui.theme.HPStatusSuccessText
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val NextChallengePeriodFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy.MM.dd")

internal val PeriodPresetDayOptions: List<Pair<Int, String>> =
    listOf(7 to "7일", 14 to "14일", 30 to "30일")

internal val CategoryIconRes: Map<String, Int> = mapOf(
    "delivery" to R.drawable.icon_delivery,
    "dining_out" to R.drawable.icon_eatout,
    "convenience" to R.drawable.icon_conv,
    "cafe" to R.drawable.icon_cafe,
    "snack" to R.drawable.icon_snack,
    "mart" to R.drawable.icon_shopping,
    "drink" to R.drawable.icon_beer,
    "etc" to R.drawable.icon_etc
)

internal fun LocalDate.toEpochMillisUtc(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

internal fun Long.toLocalDateUtc(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private fun buildRecommendationMessage(
    previousResult: ChallengeResultUiState,
    suggestedTargetAmount: Int
): AnnotatedString {
    val amountText = formatWon(previousResult.amountValue)
    return if (previousResult.status == ChallengeResultStatus.FAIL) {
        buildAnnotatedString {
            append("목표보다 ")
            withStyle(SpanStyle(color = HPMain, fontWeight = FontWeight.Bold)) {
                append(amountText)
                append(" 초과")
            }
            append("했어요!\n이번에는 조금 더 쉽게 시도해봐요. 기간을 줄이고 목표 금액을 늘린다면 성공할 수 있을 거예요!")
        }
    } else {
        buildAnnotatedString {
            append("목표보다 ")
            withStyle(SpanStyle(color = HPSub, fontWeight = FontWeight.Bold)) {
                append(amountText)
                append(" 절약")
            }
            append("했어요!\n")
            append(
                "이번에는 조금 더 타이트하게 가볼까요? 기간은 그대로 ${previousResult.totalDays}일, 목표만 ${
                    formatWon(
                        suggestedTargetAmount
                    )
                }으로 줄여서 새 기록에 도전해봐요."
            )
        }
    }
}

@Composable
fun NextChallengeRoute(
    previousResult: ChallengeResultUiState,
    suggestedTargetAmount: Int,
    onBackClick: () -> Unit,
    onStartChallengeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var periodEnabled by remember(previousResult) { mutableStateOf(false) }
    var periodDays by remember(previousResult) { mutableStateOf<Int?>(previousResult.totalDays) }
    var customPeriodDays by remember(previousResult) { mutableStateOf<Int?>(null) }
    var dateFixed by remember(previousResult) { mutableStateOf(false) }
    var startDate by remember(previousResult) { mutableStateOf<LocalDate?>(null) }
    var targetAmount by remember(suggestedTargetAmount) { mutableStateOf<Int?>(suggestedTargetAmount) }
    var selectedCategoryIds by remember { mutableStateOf(setOf("delivery")) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartConfirmDialog by remember { mutableStateOf(false) }

    val recommendationMessage = remember(previousResult, suggestedTargetAmount) {
        buildRecommendationMessage(previousResult, suggestedTargetAmount)
    }
    val effectivePeriodDays = customPeriodDays?.takeIf { it > 0 }
        ?: periodDays?.takeIf { it > 0 }
        ?: previousResult.totalDays
    val dailyGoal = when {
        periodEnabled -> (targetAmount ?: 0) / effectivePeriodDays
        startDate != null -> (targetAmount ?: 0) / 30
        else -> targetAmount ?: 0
    }
    val startDateText =
        startDate?.let { "${it.year}년 ${it.monthValue}월 ${it.dayOfMonth}일" } ?: "시작일"
    val isPeriodOrDateSelected = when {
        periodEnabled -> true
        dateFixed -> startDate != null
        else -> false
    }
    val canStartChallenge = isPeriodOrDateSelected &&
            (targetAmount ?: 0) > 0 &&
            selectedCategoryIds.isNotEmpty()

    Scaffold(
        modifier = modifier,
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

            val (heroTitle, heroSubtitle) = if (previousResult.status == ChallengeResultStatus.FAIL) {
                "괜찮아요!" to "조금 더 쉽게 도전해봐요."
            } else {
                "성공이에요!" to "한 단계 올라가 볼까요?"
            }
            NextChallengeHeroCard(title = heroTitle, subtitle = heroSubtitle)
            Spacer(modifier = Modifier.height(20.dp))
            Column {
                Text("챌린지 결과", style = Body16Bold, fontSize = 18.sp, color = HPBlack)
                Spacer(modifier = Modifier.height(10.dp))
                PreviousResultCard(result = previousResult)
            }

            Spacer(modifier = Modifier.height(10.dp))
            PochiRecommendationCard(status = previousResult.status, message = recommendationMessage)
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
                modifier = modifier
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
                modifier = modifier
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
                val days = effectivePeriodDays
                val start = if (dateFixed) (startDate ?: LocalDate.now()) else LocalDate.now()
                ChallengeRepository.startNewChallenge(
                    totalDays = days,
                    targetAmount = targetAmount ?: suggestedTargetAmount,
                    startDate = start
                )
                onStartChallengeClick()
            }
        )
    }
}

@Composable
internal fun NextChallengeHeroCard(title: String, subtitle: String, modifier: Modifier = Modifier) {
    val heroCircleDiameter = 180.dp
    val heroCircleColor = HPSub2.copy(alpha = 0.10f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(HPSub4)
            .drawBehind {
                val diameterPx = heroCircleDiameter.toPx()
                val radiusPx = diameterPx / 2f
                // top-start circle: origin (0,0) shifted by (-60dp, -40dp), then centered
                drawCircle(
                    color = heroCircleColor,
                    radius = radiusPx,
                    center = Offset(
                        x = (-80).dp.toPx() + radiusPx,
                        y = (-40).dp.toPx() + radiusPx
                    )
                )
                // bottom-end circle: bottom-right corner shifted by (50dp, 40dp), then centered
                drawCircle(
                    color = heroCircleColor,
                    radius = radiusPx,
                    center = Offset(
                        x = size.width - diameterPx + 100.dp.toPx() + radiusPx,
                        y = size.height - diameterPx + 130.dp.toPx() + radiusPx
                    )
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "NEXT CHALLENGE",
                style = MaterialTheme.typography.labelLarge,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = HPSub1
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = HPBlack
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = HPBlack
            )
        }
    }
}

@Composable
private fun ResultStatusBadge(status: ChallengeResultStatus, modifier: Modifier = Modifier) {
    val (background, textColor, label) = if (status == ChallengeResultStatus.FAIL) {
        Triple(HPStatusSuccessBg, HPStatusSuccessText, "실패")
    } else {
        Triple(HPStatusInProgressBg, HPStatusInProgressText, "성공")
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun ResultProgressBar(
    progress: Float,
    status: ChallengeResultStatus,
    modifier: Modifier = Modifier
) {
    val fillColor = if (status == ChallengeResultStatus.FAIL) HPSub else HPStatusInProgressText
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(HPGray4)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(10.dp))
                .background(fillColor)
        )
    }
}

@Composable
private fun PreviousResultCard(result: ChallengeResultUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite)
            .padding(15.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(result.title, style = Body16Bold, color = HPBlack, modifier = Modifier.weight(1f))
            ResultStatusBadge(status = result.status)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "${result.periodStart.format(NextChallengePeriodFormatter)} ~ " +
                    result.periodEnd.format(NextChallengePeriodFormatter),
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("목표", style = MaterialTheme.typography.bodySmall, color = HPText)
                Text(formatWon(result.goalAmount), fontSize = 16.sp, color = HPText)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = HPText
            )
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.Start) {
                Text("실제", style = MaterialTheme.typography.bodySmall, color = HPText)
                Text(formatWon(result.actualAmount), style = Body16Bold, color = HPMain)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        ResultProgressBar(
            progress = if (result.goalAmount > 0) result.actualAmount.toFloat() / result.goalAmount else 0f,
            status = result.status
        )
    }
}

@Composable
private fun PochiRecommendationCard(
    status: ChallengeResultStatus,
    message: AnnotatedString,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub4)
            .padding(horizontal = 20.dp, vertical = 15.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Image(
                painter = painterResource(R.drawable.img_hamster_analysis),
                contentDescription = null,
                modifier = Modifier
                    .width(80.dp)
                    .height(70.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text("포치의 추천", style = Body16Bold, color = HPSub1)
        }
        DashedDivider(color = HPSub2)
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = HPBlack
        )
    }
}

@Composable
internal fun switchColors(): SwitchColors = SwitchDefaults.colors(
    checkedThumbColor = HPWhite,
    checkedTrackColor = HPMain,
    uncheckedThumbColor = HPWhite,
    uncheckedTrackColor = HPGray5
)

@Composable
internal fun RowScope.CategoryIconChip(
    label: String,
    iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .weight(1f)
            .height(40.dp)
            .background(
                color = if (selected) HPMain else HPWhite,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = if (selected) HPMain else HPGray5,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) HPWhite else HPText,
            fontSize = 16.sp,
            maxLines = 1
        )
    }
}

@Composable
internal fun CustomPeriodDaysInput(
    value: Int?,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var hasFocusedOnce by remember { mutableStateOf(false) }
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(value) {
        if (value == null) {
            isEditing = false
            hasFocusedOnce = false
            textFieldValue = TextFieldValue("")
        }
    }
    LaunchedEffect(isEditing) {
        if (isEditing) {
            hasFocusedOnce = false
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val confirmed = value != null && !isEditing

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(43.dp)
            .background(if (confirmed) HPMain else HPWhite, RoundedCornerShape(9.5.dp))
            .border(
                width = if (confirmed) 0.dp else 1.dp,
                color = if (isEditing) HPMain else HPGray5,
                shape = RoundedCornerShape(9.5.dp)
            )
            .then(
                if (isEditing) {
                    Modifier
                } else {
                    Modifier.clickable {
                        hasFocusedOnce = false
                        isEditing = true
                    }
                }
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isEditing) {
            Box(modifier = Modifier.weight(1f)) {
                if (textFieldValue.text.isEmpty()) {
                    Text("직접 입력", style = MaterialTheme.typography.bodyMedium, color = HPText)
                }
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        val digitsOnly = newValue.text.filter(Char::isDigit)
                        textFieldValue = if (digitsOnly == newValue.text) {
                            newValue
                        } else {
                            TextFieldValue(
                                text = digitsOnly,
                                selection = TextRange(digitsOnly.length)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                hasFocusedOnce = true
                            } else if (hasFocusedOnce) {
                                isEditing = false
                            }
                        },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = HPSub1),
                    singleLine = true,
                    cursorBrush = SolidColor(HPMain),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        textFieldValue.text.toIntOrNull()?.takeIf { it > 0 }?.let(onValueChange)
                        isEditing = false
                        keyboardController?.hide()
                    })
                )
            }
            Text("일", style = MaterialTheme.typography.bodyMedium, color = HPBlack)
        } else {
            Text(
                text = if (confirmed) "${value}일" else "직접 입력",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (confirmed) FontWeight.Bold else FontWeight.Normal,
                color = if (confirmed) HPWhite else HPText,
                modifier = Modifier.weight(1f)
            )
            if (!confirmed) {
                Text("일", style = MaterialTheme.typography.bodyMedium, color = HPBlack)
            }
        }
    }
}

@Composable
internal fun ChallengeSettingsSection(
    periodEnabled: Boolean,
    onPeriodEnabledChange: (Boolean) -> Unit,
    periodDays: Int?,
    onPeriodDaysChange: (Int) -> Unit,
    customPeriodDays: Int?,
    onCustomPeriodDaysChange: (Int) -> Unit,
    dateFixed: Boolean,
    onDateFixedChange: (Boolean) -> Unit,
    startDateText: String,
    onStartDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub4)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "기간",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = HPBlack
            )
            Switch(
                checked = periodEnabled,
                onCheckedChange = onPeriodEnabledChange,
                colors = switchColors()
            )
        }
        if (periodEnabled) {
            PeriodPresetRow(
                options = PeriodPresetDayOptions,
                selectedDays = periodDays,
                onSelect = onPeriodDaysChange
            )
            CustomPeriodDaysInput(
                value = customPeriodDays,
                onValueChange = onCustomPeriodDaysChange
            )
        }

        DashedDivider(color = HPSub2)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "날짜 고정",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = HPBlack
                )
                Text(
                    "자동 갱신 챌린지를 원하나요?",
                    style = MaterialTheme.typography.labelSmall,
                    color = HPMain
                )
            }
            Switch(
                checked = dateFixed,
                onCheckedChange = onDateFixedChange,
                colors = switchColors()
            )
        }
        if (dateFixed) {
            OnboardingBulletList(
                lines = listOf(
                    "매월 선택한 날짜에 새로운 챌린지를 자동으로 시작해요.",
                    "챌린지는 한 달 동안 진행돼요."
                )
            )
            LabeledInputRow(
                label = null,
                valueText = startDateText,
                icon = Icons.Filled.CalendarToday,
                onClick = onStartDateClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
internal object FutureDatesSelectable : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        utcTimeMillis >= LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    override fun isSelectableYear(year: Int): Boolean = year >= LocalDate.now().year
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NextChallengeDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochMillisUtc(),
        selectableDates = FutureDatesSelectable
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis -> onDateSelected(millis.toLocalDateUtc()) }
            }) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Preview(showBackground = true, name = "1. 성공 다음 챌린지")
@Composable
private fun NextChallengeRouteCompletePreview() {
    HampouchTheme {
        NextChallengeRoute(
            previousResult = ChallengeResultMockData.complete,
            suggestedTargetAmount = 350_000,
            onBackClick = {},
            onStartChallengeClick = {}
        )
    }
}

@Preview(showBackground = true, name = "2. 실패 다음 챌린지")
@Composable
private fun NextChallengeRouteFailPreview() {
    HampouchTheme {
        NextChallengeRoute(
            previousResult = ChallengeResultMockData.fail,
            suggestedTargetAmount = 440_000,
            onBackClick = {},
            onStartChallengeClick = {}
        )
    }
}

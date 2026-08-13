package com.example.hampouch.ui.home.components

import android.R.attr.maxWidth
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.common.NotificationBellIcon
import com.example.hampouch.domain.model.CharacterState
import com.example.hampouch.domain.model.ExpenseEntry
import com.example.hampouch.domain.model.HomeChallenge
import com.example.hampouch.domain.model.HomeWarning
import com.example.hampouch.domain.model.MiniChallengeEntry
import com.example.hampouch.domain.model.WarningVariant
import com.example.hampouch.ui.common.ReasonTagAndAmountColumn
import com.example.hampouch.ui.home.HomeCategoryCatalog
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.tooling.preview.Preview
import com.example.hampouch.ui.theme.HampouchTheme

internal fun formatWon(amount: Int): String = "%,d".format(amount)

@Composable
fun HomeHeader(
    userName: String,
    onCalendarClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.home_greeting_format, userName),
            style = MaterialTheme.typography.titleSmall,
            color = HPBlack
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onCalendarClick) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = stringResource(R.string.cd_calendar),
                tint = HPBlack
            )
        }
        NotificationBellIcon(onClick = onNotificationClick)
    }
}

private val dateLabelFormatter = DateTimeFormatter.ofPattern("MM.dd")

@Composable
fun DateSelectorRow(
    referenceToday: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val dates = listOf(selectedDate.minusDays(1), selectedDate, selectedDate.plusDays(1))
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        dates.forEach { date ->
            val isFuture = date.isAfter(referenceToday)
            val isSelected = date == selectedDate
            DateChip(
                label = date.format(dateLabelFormatter),
                selected = isSelected,
                enabled = !isFuture,
                onClick = { onDateSelected(date) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DateChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) HPMain else Color.Transparent
    val textColor = when {
        selected -> HPWhite
        !enabled -> HPGray5
        else -> HPText
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
fun ChallengeBanner(
    challenge: HomeChallenge,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (challenge.isEnded) {
                    stringResource(R.string.home_challenge_ended_format, challenge.totalDays)
                } else {
                    stringResource(R.string.home_challenge_in_progress_format, challenge.totalDays)
                },
                style = MaterialTheme.typography.titleSmall,
                color = HPBlack,
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.home_challenge_detail_link),
                    style = MaterialTheme.typography.bodySmall,
                    color = HPText
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = HPText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(
                    R.string.home_challenge_period_format,
                    challenge.periodStartLabel,
                    challenge.periodEndLabel
                ),
                style = MaterialTheme.typography.bodySmall,
                color = HPText,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(HPMain)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (challenge.dDay == 0) {
                        stringResource(R.string.home_challenge_dday_today)
                    } else {
                        stringResource(R.string.home_challenge_dday_format, challenge.dDay)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = HPWhite
                )
            }
        }
    }
}

private fun characterDrawableRes(state: CharacterState): Int = when (state) {
    CharacterState.CHUBBY -> R.drawable.img_hamster_chubby
    CharacterState.NORMAL -> R.drawable.img_hamster_normal
    CharacterState.THIN -> R.drawable.img_hamster_thin
    CharacterState.OVER_LIMIT -> R.drawable.img_hamster_fail
}

@Composable
fun CharacterGaugeSection(challenge: HomeChallenge, modifier: Modifier = Modifier) {
    val animatedRatio by animateFloatAsState(
        targetValue = challenge.balanceRatio,
        animationSpec = tween(durationMillis = 500),
        label = "gauge_ratio"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(characterDrawableRes(challenge.characterState)),
                contentDescription = stringResource(R.string.cd_hamster_character),
                modifier = Modifier
                    .width(160.dp)
                    .height(180.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.home_limit_label_format, formatWon(challenge.dailyLimit)),
                style = MaterialTheme.typography.bodySmall,
                color = HPText
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(HPGray4)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animatedRatio.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(50))
                    .background(if (challenge.isOverLimit) HPSub else HPMain)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_today_balance_label),
                style = MaterialTheme.typography.bodyMedium,
                color = HPText
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.home_amount_won_format, formatWon(challenge.todayBalance)),
                style = MaterialTheme.typography.titleSmall,
                color = if (challenge.isOverLimit) HPSub else HPBlack
            )
        }
    }
}

private const val StreakBoxWidthDeltaRatio = 0.2f

@Composable
fun SavingsStreakRow(savedAmount: Int, streakDays: Int, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val gap = 12.dp
        val halfWidth = (maxWidth - gap) / 2
        val widthDelta = halfWidth * StreakBoxWidthDeltaRatio
        val savingsBoxWidth = halfWidth + widthDelta
        val streakBoxWidth = halfWidth - widthDelta

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            Column(
                modifier = Modifier
                    .width(savingsBoxWidth)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Transparent)
                    .border(width = 1.dp, color = HPMain, shape = RoundedCornerShape(16.dp))
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.home_saved_amount_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = HPText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (savedAmount >= 0) {
                        stringResource(R.string.home_saved_amount_format, formatWon(savedAmount))
                    } else {
                        stringResource(R.string.home_saved_amount_negative_format, formatWon(savedAmount))
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = if (savedAmount >= 0) HPMain else HPSub
                )
            }
            Row(
                modifier = Modifier
                    .width(streakBoxWidth)
                    .clip(RoundedCornerShape(16.dp))
                    .background(HPMain)
                    .padding(horizontal = 6.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = HPWhite,
                    modifier = Modifier
                        .offset(x = (-1).dp)
                        .size(32.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = stringResource(R.string.home_streak_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = HPWhite
                    )
                    Text(
                        text = stringResource(R.string.home_streak_days_format, streakDays),
                        style = MaterialTheme.typography.titleSmall,
                        color = HPWhite
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onViewAllClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleSmall, color = HPBlack)
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onViewAllClick) {
            Text(
                text = stringResource(R.string.home_view_all),
                style = MaterialTheme.typography.bodySmall,
                color = HPText
            )
        }
    }
}

@Composable
fun TodayExpenseSection(
    expenses: List<ExpenseEntry>,
    onViewAllClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    modifier: Modifier = Modifier,
    onExpenseClick: (String) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = stringResource(R.string.home_today_expense_title), onViewAllClick = onViewAllClick)
        if (expenses.isEmpty()) {
            EmptyStateBlock(
                title = stringResource(R.string.home_expense_empty_title),
                subtitle = stringResource(R.string.home_expense_empty_subtitle)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                expenses.forEach { entry -> ExpenseItemCard(entry, onClick = { onExpenseClick(entry.id) }) }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onAddExpenseClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HPMain, contentColor = HPWhite)
        ) {
            Text(stringResource(R.string.home_expense_input_button), style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
fun ExpenseItemCard(entry: ExpenseEntry, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val category = HomeCategoryCatalog.byId(entry.categoryId)
    val icon = category?.icon ?: HomeCategoryCatalog.defaultIcon
    val accentColor = category?.accentColor ?: HomeCategoryCatalog.defaultColor
    // 카테고리를 고르지 않았으면 줄 자체를 그리지 않는다. "기타"는 사용자가 명시적으로 고른 경우에만 나온다.
    val categoryLabel = entry.customCategoryName
        ?: category?.let { stringResource(it.labelResId) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HPWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (entry.name != null) {
                Text(text = entry.name, style = MaterialTheme.typography.bodyMedium, color = HPBlack, fontWeight = FontWeight.Bold)
            }
            if (categoryLabel != null) {
                Text(text = categoryLabel, style = MaterialTheme.typography.bodySmall, color = HPText)
            }
        }
        ReasonTagAndAmountColumn(
            reasonTag = entry.reasonTag,
            amountText = stringResource(R.string.home_amount_won_format, formatWon(entry.amount))
        )
    }
}

@Composable
fun MiniChallengeSection(
    items: List<MiniChallengeEntry>,
    onViewAllClick: () -> Unit,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = stringResource(R.string.home_mini_challenge_title), onViewAllClick = onViewAllClick)
        if (items.isEmpty()) {
            EmptyStateBlock(title = stringResource(R.string.home_mini_challenge_empty))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items.forEach { item -> MiniChallengeRow(item = item, onToggle = { onToggle(item.id) }) }
            }
        }
    }
}

@Composable
private fun MiniChallengeRow(item: MiniChallengeEntry, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HPWhite)
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (item.isChecked) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
            contentDescription = null,
            tint = if (item.isChecked) HPMain else HPGray5,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (item.isChecked) HPText else HPBlack
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = item.periodLabel(),
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )
    }
}

@Composable
fun EmptyStateBlock(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = HPText)
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = HPText)
        }
    }
}

@Composable
fun WarningBannerList(
    warnings: List<HomeWarning>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        warnings.forEach { warning ->
            WarningBannerCard(
                warning = warning,
                onClick = { onSuggestionClick(warning.id) }
            )
        }
    }
}

@Composable
private fun WarningBannerCard(
    warning: HomeWarning,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAlert = warning.variant == WarningVariant.ALERT
    val cardBackground = if (isAlert) HPSub.copy(alpha = 0.12f) else HPWhite
    val titleColor = if (isAlert) HPSub else HPBlack
    val messageColor = if (isAlert) HPSub else HPText

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground)
            .border(width = if (isAlert) 0.dp else 1.dp, color = HPGray4, shape = RoundedCornerShape(16.dp))
            .then(if (isAlert) Modifier else Modifier.clickable(onClick = onClick))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isAlert) {
            Image(
                painter = painterResource(R.drawable.icon_warning),
                contentDescription = stringResource(R.string.cd_warning_icon),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = warning.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = titleColor)
            Text(text = warning.message, style = MaterialTheme.typography.bodySmall, color = messageColor)
        }
        if (!isAlert) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = HPText
            )
        }
    }
}

@Composable
fun NoActiveChallengeSection(onStartChallengeClick: () -> Unit, modifier: Modifier = Modifier) {
    val baseTitleFontSize = MaterialTheme.typography.titleSmall.fontSize
    var titleFontSize by remember(baseTitleFontSize) { mutableStateOf(baseTitleFontSize) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub4)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 40.dp)
        ) {
            Text(
                text = stringResource(R.string.home_no_challenge_title),
                style = MaterialTheme.typography.titleSmall.copy(fontSize = titleFontSize),
                color = HPBlack,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                onTextLayout = { result ->
                    if (result.didOverflowWidth) {
                        titleFontSize *= 0.92f
                    }
                }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.home_no_challenge_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = HPText
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.home_no_challenge_cta),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = HPMain,
                modifier = Modifier.clickable(onClick = onStartChallengeClick)
            )
        }
        Image(
            painter = painterResource(R.drawable.img_hamster_normal),
            contentDescription = stringResource(R.string.cd_hamster_character),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp)
                .offset(y = 28.dp)
                .width(110.dp)
                .height(128.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF1F1F1, name = "지출 카드 - 입력 조합별")
@Composable
private fun ExpenseItemCardBranchPreview() {
    HampouchTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 이름 + 카테고리 + 이유
            ExpenseItemCard(
                ExpenseEntry(id = "1", categoryId = "cafe", name = "스타벅스", reasonTag = "스트레스", amount = 4_500),
                onClick = {}
            )
            // 이유 없음
            ExpenseItemCard(
                ExpenseEntry(id = "2", categoryId = "cafe", name = "스타벅스", amount = 4_500),
                onClick = {}
            )
            // 카테고리 미선택
            ExpenseItemCard(
                ExpenseEntry(id = "3", name = "스타벅스", reasonTag = "스트레스", amount = 4_500),
                onClick = {}
            )
            // '기타'를 명시적으로 선택
            ExpenseItemCard(
                ExpenseEntry(id = "4", categoryId = "etc", name = "스타벅스", reasonTag = "스트레스", amount = 4_500),
                onClick = {}
            )
            // 이름 없음
            ExpenseItemCard(
                ExpenseEntry(id = "5", categoryId = "cafe", reasonTag = "스트레스", amount = 4_500),
                onClick = {}
            )
            // 이름·카테고리 없음
            ExpenseItemCard(
                ExpenseEntry(id = "6", reasonTag = "스트레스", amount = 4_500),
                onClick = {}
            )
            // 전부 없음
            ExpenseItemCard(
                ExpenseEntry(id = "7", amount = 4_500),
                onClick = {}
            )
            // 직접 입력 카테고리 + 직접 입력 이유
            ExpenseItemCard(
                ExpenseEntry(id = "8", customCategoryName = "직접입력한카테고리", name = "지출내역", reasonTag = "감정태깅", amount = 0),
                onClick = {}
            )
        }
    }
}

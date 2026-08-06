package com.example.hampouch.ui.expensedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.data.model.ExpenseCalendarViewMode
import com.example.hampouch.data.model.ExpenseChallengePeriod
import com.example.hampouch.data.model.ExpenseRecord
import com.example.hampouch.ui.common.ReasonTagAndAmountColumn
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private fun expenseInputEnabled(
    period: ExpenseChallengePeriod?,
    referenceToday: LocalDate,
    selectedDate: LocalDate,
    restrictToChallengePeriod: Boolean
): Boolean {
    if (period == null) return false
    if (restrictToChallengePeriod) return period.isActiveOn(selectedDate)
    return !referenceToday.isBefore(period.startDate) && !referenceToday.isAfter(period.endDate) &&
        !selectedDate.isBefore(period.startDate) && !selectedDate.isAfter(referenceToday)
}

private fun weekGridStart(date: LocalDate): LocalDate {
    val offset = date.dayOfWeek.value % 7
    return date.minusDays(offset.toLong())
}

private fun monthGridStart(date: LocalDate): LocalDate {
    val firstOfMonth = date.withDayOfMonth(1)
    return weekGridStart(firstOfMonth)
}

private fun weekOfMonthOrdinal(weekStart: LocalDate): Int {
    val gridStart = monthGridStart(weekStart)
    return ((weekStart.toEpochDay() - gridStart.toEpochDay()) / 7).toInt() + 1
}

@Composable
fun ExpenseCalendarRoute(
    onBackClick: () -> Unit,
    onExpenseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    referenceToday: LocalDate = LocalDate.now(),
    challengePeriod: ExpenseChallengePeriod? = ExpenseDetailMockData.activeChallengePeriod(),
    monthlyTotal: Int = ExpenseDetailMockData.monthlyTotal(),
    monthlyDailyAverage: Int = ExpenseDetailMockData.monthlyDailyAverage(),
    weeklyTotal: Int = ExpenseDetailMockData.weeklyTotal(),
    weeklyDailyAverage: Int = ExpenseDetailMockData.weeklyDailyAverage(),
    onExpenseAnalysisClick: () -> Unit = {},
    onAddExpenseClick: (LocalDate) -> Unit = {},
    restrictToChallengePeriod: Boolean = false
) {
    val initialSelectedDate = if (restrictToChallengePeriod) {
        challengePeriod?.endDate ?: referenceToday
    } else {
        referenceToday
    }
    var viewMode by remember { mutableStateOf(ExpenseCalendarViewMode.MONTHLY) }
    var selectedDate by remember { mutableStateOf(initialSelectedDate) }
    var displayedMonth by remember { mutableStateOf(initialSelectedDate.withDayOfMonth(1)) }
    var displayedWeekStart by remember { mutableStateOf(weekGridStart(referenceToday)) }
    val summaryByDate = ExpenseDetailStore.recordsById.values
        .groupBy { it.date }
        .mapValues { (_, records) -> records.sumOf { it.amount } }
    val dayRecords = ExpenseDetailStore.recordsForDate(selectedDate)
    val inputEnabled = expenseInputEnabled(challengePeriod, referenceToday, selectedDate, restrictToChallengePeriod)
    val challengeEnded = challengePeriod != null && referenceToday.isAfter(challengePeriod.endDate)
    val editableRange = challengePeriod.takeIf { restrictToChallengePeriod }

    Scaffold(
        modifier = modifier,
        topBar = {
            ExpenseCalendarTopBar(
                title = stringResource(
                    R.string.expensedetail_calendar_year_month_format,
                    displayedMonth.year,
                    displayedMonth.monthValue
                ),
                onBackClick = onBackClick,
                onAnalysisClick = onExpenseAnalysisClick,
                onPreviousMonth = { displayedMonth = displayedMonth.minusMonths(1) },
                onNextMonth = { displayedMonth = displayedMonth.plusMonths(1) },
                showAnalysisAction = !restrictToChallengePeriod
            )
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
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(HPSub4)
                    .padding(20.dp)
            ) {
                if (!restrictToChallengePeriod) {
                    CalendarViewModeToggle(
                        viewMode = viewMode,
                        onViewModeChange = { viewMode = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (viewMode == ExpenseCalendarViewMode.MONTHLY) {
                    CalendarStatCard(
                        totalLabel = stringResource(
                            R.string.expensedetail_calendar_monthly_total_format,
                            displayedMonth.monthValue
                        ),
                        totalAmount = monthlyTotal,
                        dailyAverage = monthlyDailyAverage
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    MonthCalendarGrid(
                        month = displayedMonth,
                        referenceToday = referenceToday,
                        selectedDate = selectedDate,
                        summaryByDate = summaryByDate,
                        onDateSelected = { selectedDate = it },
                        editableRange = editableRange
                    )
                } else {
                    CalendarStatCard(
                        totalLabel = stringResource(R.string.expensedetail_calendar_weekly_total),
                        totalAmount = weeklyTotal,
                        dailyAverage = weeklyDailyAverage
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    WeekNavigator(
                        weekStart = displayedWeekStart,
                        onPreviousWeek = { displayedWeekStart = displayedWeekStart.minusWeeks(1) },
                        onNextWeek = { displayedWeekStart = displayedWeekStart.plusWeeks(1) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    WeekCalendarRow(
                        weekStart = displayedWeekStart,
                        referenceToday = referenceToday,
                        selectedDate = selectedDate,
                        summaryByDate = summaryByDate,
                        onDateSelected = { selectedDate = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            SelectedDayHeader(selectedDate = selectedDate, referenceToday = referenceToday, dayRecords = dayRecords)
            Spacer(modifier = Modifier.height(10.dp))
            if (dayRecords.isEmpty()) {
                Text(
                    stringResource(R.string.expensedetail_calendar_day_empty_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HPText,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    dayRecords.forEach { record ->
                        ExpenseCalendarListItem(record = record, onClick = { onExpenseClick(record.id) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { if (inputEnabled) onAddExpenseClick(selectedDate) },
                enabled = inputEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (inputEnabled) HPMain else HPGray4,
                    disabledContainerColor = HPGray4,
                    contentColor = if (inputEnabled) HPWhite else HPText,
                    disabledContentColor = HPText
                )
            ) {
                Text(stringResource(R.string.home_expense_input_button), style = MaterialTheme.typography.titleSmall)
            }
            if (!inputEnabled && challengeEnded && !restrictToChallengePeriod) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.expensedetail_calendar_input_disabled_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = HPText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseCalendarTopBar(
    title: String,
    onBackClick: () -> Unit,
    onAnalysisClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
    showAnalysisAction: Boolean = true
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        Icons.Filled.ChevronLeft,
                        contentDescription = stringResource(R.string.cd_calendar_month_prev),
                        tint = HPBlack
                    )
                }
                Text(title, style = MaterialTheme.typography.titleSmall, color = HPBlack)
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = HPBlack)
                IconButton(onClick = onNextMonth) {
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = stringResource(R.string.cd_calendar_month_next),
                        tint = HPBlack
                    )
                }
            }
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
        actions = {
            if (showAnalysisAction) {
                IconButton(onClick = onAnalysisClick) {
                    Icon(
                        Icons.Filled.BarChart,
                        contentDescription = stringResource(R.string.cd_expense_analysis),
                        tint = HPBlack
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = HPGray2)
    )
}

@Composable
private fun CalendarViewModeToggle(
    viewMode: ExpenseCalendarViewMode,
    onViewModeChange: (ExpenseCalendarViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(HPWhite.copy(alpha = 0.4f))
            .padding(4.dp)
    ) {
        CalendarToggleSegment(
            label = stringResource(R.string.expensedetail_calendar_monthly_tab),
            selected = viewMode == ExpenseCalendarViewMode.MONTHLY,
            onClick = { onViewModeChange(ExpenseCalendarViewMode.MONTHLY) },
            modifier = Modifier.weight(1f)
        )
        CalendarToggleSegment(
            label = stringResource(R.string.expensedetail_calendar_weekly_tab),
            selected = viewMode == ExpenseCalendarViewMode.WEEKLY,
            onClick = { onViewModeChange(ExpenseCalendarViewMode.WEEKLY) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CalendarToggleSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) HPWhite else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) HPBlack else HPText
        )
    }
}

@Composable
private fun CalendarStatCard(
    totalLabel: String,
    totalAmount: Int,
    dailyAverage: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HPWhite)
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(totalLabel, style = MaterialTheme.typography.bodySmall, color = HPText)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            stringResource(R.string.expensedetail_amount_won_format, formatWon(totalAmount)),
            style = MaterialTheme.typography.titleSmall,
            color = HPMain
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.expensedetail_calendar_daily_average_format, formatWon(dailyAverage)),
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )
    }
}

private val weekdayLabels: List<DayOfWeek> = listOf(
    DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
)

@Composable
private fun WeekdayHeaderRow(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        weekdayLabels.forEach { day ->
            Text(
                day.getDisplayName(TextStyle.NARROW, Locale.KOREA),
                style = MaterialTheme.typography.labelMedium,
                color = HPText,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MonthCalendarGrid(
    month: LocalDate,
    referenceToday: LocalDate,
    selectedDate: LocalDate,
    summaryByDate: Map<LocalDate, Int>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    editableRange: ExpenseChallengePeriod? = null
) {
    val gridStart = monthGridStart(month)
    val monthValue = month.monthValue
    Column(modifier = modifier.fillMaxWidth()) {
        WeekdayHeaderRow()
        Spacer(modifier = Modifier.height(6.dp))
        repeat(6) { weekIndex ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { dayIndex ->
                    val date = gridStart.plusDays((weekIndex * 7 + dayIndex).toLong())
                    val isEditable = editableRange == null || editableRange.isActiveOn(date)
                    CalendarDayCell(
                        date = date,
                        isCurrentMonth = date.monthValue == monthValue,
                        referenceToday = referenceToday,
                        selected = date == selectedDate,
                        amount = summaryByDate[date],
                        onClick = { if (!date.isAfter(referenceToday) && isEditable) onDateSelected(date) },
                        modifier = Modifier.weight(1f),
                        forceMutedColor = !isEditable
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekCalendarRow(
    weekStart: LocalDate,
    referenceToday: LocalDate,
    selectedDate: LocalDate,
    summaryByDate: Map<LocalDate, Int>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        WeekdayHeaderRow()
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            repeat(7) { dayIndex ->
                val date = weekStart.plusDays(dayIndex.toLong())
                CalendarDayCell(
                    date = date,
                    isCurrentMonth = true,
                    referenceToday = referenceToday,
                    selected = date == selectedDate,
                    amount = summaryByDate[date],
                    onClick = { if (!date.isAfter(referenceToday)) onDateSelected(date) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    referenceToday: LocalDate,
    selected: Boolean,
    amount: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    forceMutedColor: Boolean = false
) {
    val isFuture = date.isAfter(referenceToday)
    val numberColor = when {
        forceMutedColor -> HPText
        isFuture -> HPText
        !isCurrentMonth -> HPSub2
        else -> HPBlack
    }
    Column(
        modifier = modifier
            .clickable(enabled = !isFuture && !forceMutedColor, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (selected) HPSub2 else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = numberColor
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            amount?.let { formatWon(it) } ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = HPMain,
            maxLines = 1
        )
    }
}

@Composable
private fun WeekNavigator(
    weekStart: LocalDate,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ordinals = stringArrayResource(R.array.expensedetail_week_ordinals)
    val ordinalIndex = (weekOfMonthOrdinal(weekStart) - 1).coerceIn(0, ordinals.size - 1)
    val weekEnd = weekStart.plusDays(6)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPreviousWeek) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.cd_calendar_week_prev), tint = HPBlack)
            }
            Text(
                stringResource(R.string.expensedetail_calendar_week_label_format, weekStart.monthValue, ordinals[ordinalIndex]),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = HPBlack
            )
            IconButton(onClick = onNextWeek) {
                Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.cd_calendar_week_next), tint = HPBlack)
            }
        }
        Text(
            stringResource(
                R.string.expensedetail_calendar_week_range_format,
                weekStart.monthValue, weekStart.dayOfMonth, weekEnd.monthValue, weekEnd.dayOfMonth
            ),
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )
    }
}

@Composable
private fun SelectedDayHeader(
    selectedDate: LocalDate,
    referenceToday: LocalDate,
    dayRecords: List<ExpenseRecord>,
    modifier: Modifier = Modifier
) {
    val total = dayRecords.sumOf { it.amount }
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (selectedDate == referenceToday) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(HPMain)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    stringResource(R.string.expensedetail_calendar_today_badge),
                    style = MaterialTheme.typography.labelMedium,
                    color = HPWhite
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            stringResource(
                R.string.expensedetail_calendar_selected_day_format,
                selectedDate.monthValue,
                selectedDate.dayOfMonth,
                selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREA)
            ),
            style = MaterialTheme.typography.titleSmall,
            color = HPBlack
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            stringResource(R.string.expensedetail_amount_won_format, formatWon(total)),
            style = MaterialTheme.typography.titleSmall,
            color = HPMain
        )
    }
}

@Composable
private fun ExpenseCalendarListItem(record: ExpenseRecord, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val categoryLabel = resolveCategoryLabel(record.categoryId, record.customCategoryName)
    val reasonLabel = resolveReasonLabel(record.reasonId, record.customReason)
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
                .background(resolveCategoryColor(record.categoryId).copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = resolveCategoryIcon(record.categoryId),
                contentDescription = null,
                tint = resolveCategoryColor(record.categoryId),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                record.expenseName ?: categoryLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = HPBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                categoryLabel,
                style = MaterialTheme.typography.bodySmall,
                color = HPText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        ReasonTagAndAmountColumn(
            reasonTag = reasonLabel,
            amountText = stringResource(R.string.expensedetail_amount_won_format, formatWon(record.amount))
        )
    }
}

@Preview(showBackground = true, name = "8. 지출 캘린더 (월간)")
@Composable
private fun ExpenseCalendarMonthlyPreview() {
    HampouchTheme {
        ExpenseCalendarRoute(onBackClick = {}, onExpenseClick = {})
    }
}

@Preview(showBackground = true, name = "9. 지출 캘린더 (지출 없는 날)")
@Composable
private fun ExpenseCalendarEmptyDayPreview() {
    val today = LocalDate.now()
    HampouchTheme {
        ExpenseCalendarRoute(
            onBackClick = {},
            onExpenseClick = {},
            referenceToday = today
        )
    }
}

@Preview(showBackground = true, name = "10. 지출 캘린더 (챌린지 종료)")
@Composable
private fun ExpenseCalendarChallengeEndedPreview() {
    val today = remember { LocalDate.now() }
    HampouchTheme {
        ExpenseCalendarRoute(
            onBackClick = {},
            onExpenseClick = {},
            referenceToday = today,
            challengePeriod = ExpenseDetailMockData.endedChallengePeriod(today)
        )
    }
}

@Preview(showBackground = true, name = "11. 지출 캘린더 (주간)")
@Composable
private fun ExpenseCalendarWeeklyPreview() {
    HampouchTheme {
        ExpenseCalendarRoute(onBackClick = {}, onExpenseClick = {})
    }
}

@Preview(showBackground = true, name = "12. 지출 캘린더 (챌린지 종료 후 수정 - 제한 모드)")
@Composable
private fun ExpenseCalendarChallengeEndEditPreview() {
    val today = remember { LocalDate.now() }
    HampouchTheme {
        ExpenseCalendarRoute(
            onBackClick = {},
            onExpenseClick = {},
            referenceToday = today,
            challengePeriod = ExpenseDetailMockData.endedChallengePeriod(today),
            restrictToChallengePeriod = true
        )
    }
}

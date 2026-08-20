package com.example.hampouch.ui.expensedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.unit.sp
import com.example.hampouch.R
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hampouch.core.config.ExpenseConfig
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.DailyAmount
import com.example.hampouch.domain.model.ExpensePeriodSummary
import com.example.hampouch.domain.model.ExpenseCalendarViewMode
import com.example.hampouch.domain.model.ExpenseChallengePeriod
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.ui.common.InlineLoadErrorCard
import com.example.hampouch.ui.common.InlineLoadingIndicator
import com.example.hampouch.ui.common.LoadState
import com.example.hampouch.ui.common.ReasonTagAndAmountColumn
import com.example.hampouch.ui.common.StaleDataRefreshBanner
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.expenseinput.canChangeExpenseOn
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private fun expenseInputEnabled(
    challengeState: ChallengeState,
    period: ExpenseChallengePeriod?,
    referenceToday: LocalDate,
    selectedDate: LocalDate,
    restrictToChallengePeriod: Boolean
): Boolean {
    if (restrictToChallengePeriod) return period != null && period.isActiveOn(selectedDate)
    return challengeState.canChangeExpenseOn(selectedDate, referenceToday)
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
    challengePeriod: ExpenseChallengePeriod? = null,
    onExpenseAnalysisClick: () -> Unit,
    onAddExpenseClick: (LocalDate) -> Unit,
    restrictToChallengePeriod: Boolean = false,
    viewModel: ExpenseCalendarViewModel = hiltViewModel()
) {
    val calendarChallengeState by viewModel.challengeState.collectAsStateWithLifecycle()
    val restState by viewModel.restState.collectAsStateWithLifecycle()
    val effectiveChallengePeriod = challengePeriod
        ?: ExpenseDetailMockData.activeChallengePeriod(calendarChallengeState)
    val initialSelectedDate = if (restrictToChallengePeriod) {
        effectiveChallengePeriod?.endDate ?: referenceToday
    } else {
        referenceToday
    }
    var viewMode by rememberSaveable { mutableStateOf(ExpenseCalendarViewMode.MONTHLY) }
    var selectedDate by rememberSaveable { mutableStateOf(initialSelectedDate) }
    var displayedMonth by rememberSaveable { mutableStateOf(initialSelectedDate.withDayOfMonth(1)) }
    var displayedWeekStart by rememberSaveable { mutableStateOf(weekGridStart(referenceToday)) }

    val records by viewModel.records.collectAsStateWithLifecycle()
    val monthSummary by viewModel.monthSummary.collectAsStateWithLifecycle()
    val weekSummary by viewModel.weekSummary.collectAsStateWithLifecycle()
    val monthLoadState by viewModel.monthLoadState.collectAsStateWithLifecycle()
    val weekLoadState by viewModel.weekLoadState.collectAsStateWithLifecycle()
    val dayLoadState by viewModel.dayLoadState.collectAsStateWithLifecycle()
    LaunchedEffect(displayedMonth) { viewModel.loadMonthSummary(YearMonth.from(displayedMonth)) }
    LaunchedEffect(displayedWeekStart) { viewModel.loadWeekSummary(displayedWeekStart) }
    LaunchedEffect(selectedDate) { viewModel.loadDay(selectedDate) }

    ExpenseCalendarContent(
        referenceToday = referenceToday,
        challengeState = calendarChallengeState,
        effectiveChallengePeriod = effectiveChallengePeriod,
        restrictToChallengePeriod = restrictToChallengePeriod,
        viewMode = viewMode,
        onViewModeChange = { viewMode = it },
        selectedDate = selectedDate,
        onDateSelected = { selectedDate = it },
        displayedMonth = displayedMonth,
        onPreviousMonth = {
            displayedMonth = displayedMonth.minusMonths(1)
            selectedDate = displayedMonth
        },
        onNextMonth = {
            displayedMonth = displayedMonth.plusMonths(1)
            selectedDate = displayedMonth
        },
        displayedWeekStart = displayedWeekStart,
        onPreviousWeek = { displayedWeekStart = displayedWeekStart.minusWeeks(1) },
        onNextWeek = { displayedWeekStart = displayedWeekStart.plusWeeks(1) },
        records = records,
        monthSummary = monthSummary,
        weekSummary = weekSummary,
        monthLoadState = monthLoadState,
        weekLoadState = weekLoadState,
        dayLoadState = dayLoadState,
        isResting = restState.isResting,
        onBackClick = onBackClick,
        onExpenseClick = onExpenseClick,
        onExpenseAnalysisClick = onExpenseAnalysisClick,
        onAddExpenseClick = onAddExpenseClick,
        onRetryMonth = viewModel::retryMonth,
        onRetryWeek = viewModel::retryWeek,
        onRetryDay = viewModel::retryDay,
        modifier = modifier
    )
}

@Composable
private fun ExpenseCalendarContent(
    referenceToday: LocalDate,
    challengeState: ChallengeState,
    effectiveChallengePeriod: ExpenseChallengePeriod?,
    restrictToChallengePeriod: Boolean,
    viewMode: ExpenseCalendarViewMode,
    onViewModeChange: (ExpenseCalendarViewMode) -> Unit,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    displayedMonth: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    displayedWeekStart: LocalDate,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    records: Map<String, ExpenseRecord>,
    monthSummary: ExpensePeriodSummary?,
    weekSummary: ExpensePeriodSummary?,
    monthLoadState: LoadState,
    weekLoadState: LoadState,
    dayLoadState: LoadState,
    isResting: Boolean,
    onBackClick: () -> Unit,
    onExpenseClick: (String) -> Unit,
    onExpenseAnalysisClick: () -> Unit,
    onAddExpenseClick: (LocalDate) -> Unit,
    onRetryMonth: () -> Unit,
    onRetryWeek: () -> Unit,
    onRetryDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCalendarLoadState = if (viewMode == ExpenseCalendarViewMode.WEEKLY) weekLoadState else monthLoadState
    val activeCalendarHasData = if (viewMode == ExpenseCalendarViewMode.WEEKLY) weekSummary != null else monthSummary != null

    val activeSummary = if (viewMode == ExpenseCalendarViewMode.WEEKLY) weekSummary else monthSummary
    val summaryByDate = activeSummary?.dailyBreakdown?.associate { it.date to it.amount }.orEmpty()
    val dayRecords = records.values.filter { it.date == selectedDate }.asReversed()
    val inputEnabled = expenseInputEnabled(
        challengeState,
        effectiveChallengePeriod,
        referenceToday,
        selectedDate,
        restrictToChallengePeriod
    )
    val challengeEnded = !isResting &&
        effectiveChallengePeriod != null && referenceToday.isAfter(effectiveChallengePeriod.endDate)
    val editableRange = effectiveChallengePeriod.takeIf { restrictToChallengePeriod }

    val monthlyTotal = monthSummary?.totalAmount ?: 0
    val monthlyDailyAverage = monthSummary?.dailyAverage ?: 0

    val weeklyTotal = weekSummary?.totalAmount ?: 0
    val weeklyDailyAverage = weekSummary?.dailyAverage ?: 0

    val topBarYearMonth = if (viewMode == ExpenseCalendarViewMode.WEEKLY) displayedWeekStart else displayedMonth

    Scaffold(
        modifier = modifier,
        topBar = {
            ExpenseCalendarTopBar(
                title = stringResource(
                    R.string.expensedetail_calendar_year_month_format,
                    topBarYearMonth.year,
                    topBarYearMonth.monthValue
                ),
                onBackClick = onBackClick,
                onAnalysisClick = onExpenseAnalysisClick,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                monthNavEnabled = viewMode == ExpenseCalendarViewMode.MONTHLY,
                showAnalysisAction = !restrictToChallengePeriod
            )
        },
        containerColor = HPGray2
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            item(contentType = "calendar") {
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(HPSub4)
                        .padding(5.dp)
                        .padding(top = 5.dp)
                ) {
                if (!restrictToChallengePeriod) {
                    CalendarViewModeToggle(
                        viewMode = viewMode,
                        onViewModeChange = onViewModeChange
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                val calendarFailedWithNoData = activeCalendarLoadState is LoadState.Failure && !activeCalendarHasData
                val calendarStaleFailureMessage = (activeCalendarLoadState as? LoadState.Failure)
                    ?.message
                    ?.takeIf { activeCalendarHasData }
                if (calendarStaleFailureMessage != null) {
                    StaleDataRefreshBanner(
                        message = calendarStaleFailureMessage,
                        onRetry = { if (viewMode == ExpenseCalendarViewMode.WEEKLY) onRetryWeek() else onRetryMonth() }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                if (calendarFailedWithNoData) {
                    InlineLoadErrorCard(
                        message = (activeCalendarLoadState as LoadState.Failure).message,
                        onRetry = { if (viewMode == ExpenseCalendarViewMode.WEEKLY) onRetryWeek() else onRetryMonth() }
                    )
                } else if (viewMode == ExpenseCalendarViewMode.MONTHLY) {
                    CalendarStatCard(
                        totalLabel = stringResource(
                            R.string.expensedetail_calendar_monthly_total_format,
                            displayedMonth.monthValue
                        ),
                        totalAmount = monthlyTotal,
                        dailyAverage = monthlyDailyAverage,
                        isLoading = activeCalendarLoadState is LoadState.Loading && !activeCalendarHasData
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    MonthCalendarGrid(
                        month = displayedMonth,
                        referenceToday = referenceToday,
                        selectedDate = selectedDate,
                        summaryByDate = summaryByDate,
                        onDateSelected = onDateSelected,
                        editableRange = editableRange
                    )
                } else {
                    val isCurrentWeek = displayedWeekStart == weekGridStart(referenceToday)
                    val weekOrdinals = stringArrayResource(R.array.expensedetail_week_ordinals)
                    val weekOrdinalIndex = (weekOfMonthOrdinal(displayedWeekStart) - 1).coerceIn(0, weekOrdinals.size - 1)
                    CalendarStatCard(
                        totalLabel = if (isCurrentWeek) {
                            stringResource(R.string.expensedetail_calendar_weekly_total)
                        } else {
                            stringResource(
                                R.string.expensedetail_calendar_weekly_total_format,
                                displayedWeekStart.monthValue,
                                weekOrdinals[weekOrdinalIndex]
                            )
                        },
                        totalAmount = weeklyTotal,
                        dailyAverage = weeklyDailyAverage,
                        isLoading = activeCalendarLoadState is LoadState.Loading && !activeCalendarHasData
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    WeekNavigator(
                        weekStart = displayedWeekStart,
                        onPreviousWeek = onPreviousWeek,
                        onNextWeek = onNextWeek
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    WeekCalendarRow(
                        weekStart = displayedWeekStart,
                        referenceToday = referenceToday,
                        selectedDate = selectedDate,
                        summaryByDate = summaryByDate,
                        onDateSelected = onDateSelected
                    )
                }
                }
                Spacer(modifier = Modifier.height(15.dp))
                SelectedDayHeader(selectedDate = selectedDate, referenceToday = referenceToday, dayRecords = dayRecords)
                Spacer(modifier = Modifier.height(10.dp))
            }
            val dayFailedWithNoData = dayLoadState is LoadState.Failure && dayRecords.isEmpty()
            val dayLoadingWithNoData = dayLoadState is LoadState.Loading && dayRecords.isEmpty()
            val dayStaleFailureMessage = (dayLoadState as? LoadState.Failure)?.message?.takeIf { dayRecords.isNotEmpty() }
            if (dayStaleFailureMessage != null) {
                item(contentType = "load_state") {
                    StaleDataRefreshBanner(
                        message = dayStaleFailureMessage,
                        onRetry = onRetryDay,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
            }
            if (dayFailedWithNoData) {
                item(contentType = "load_state") {
                    InlineLoadErrorCard(
                        message = (dayLoadState as LoadState.Failure).message,
                        onRetry = onRetryDay
                    )
                }
            } else if (dayLoadingWithNoData) {
                item(contentType = "load_state") { InlineLoadingIndicator() }
            } else if (dayRecords.isEmpty()) {
                item(contentType = "empty_state") {
                    Text(
                        stringResource(R.string.expensedetail_calendar_day_empty_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = HPText,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(dayRecords, key = { it.id }, contentType = { "expense_record" }) { record ->
                    ExpenseCalendarListItem(record = record, onClick = { onExpenseClick(record.id) })
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            item(contentType = "action") {
                Spacer(modifier = Modifier.height(10.dp))
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
                    Text(
                        stringResource(R.string.home_expense_input_button),
                        style = MaterialTheme.typography.titleSmall
                    )
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
}

@Composable
private fun ExpenseCalendarTopBar(
    title: String,
    onBackClick: () -> Unit,
    onAnalysisClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
    monthNavEnabled: Boolean = true,
    showAnalysisAction: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(HPGray2)
            .padding(horizontal = 20.dp)
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = HPBlack
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth, enabled = monthNavEnabled) {
                Icon(
                    Icons.Filled.ChevronLeft,
                    contentDescription = stringResource(R.string.cd_calendar_month_prev),
                    tint = if (monthNavEnabled) HPBlack else HPGray4
                )
            }
            Text(title, style = MaterialTheme.typography.titleSmall, color = HPBlack)
            IconButton(onClick = onNextMonth, enabled = monthNavEnabled) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = stringResource(R.string.cd_calendar_month_next),
                    tint = if (monthNavEnabled) HPBlack else HPGray4
                )
            }
        }
        if (showAnalysisAction) {
            IconButton(onClick = onAnalysisClick) {
                Icon(
                    Icons.Filled.BarChart,
                    contentDescription = stringResource(R.string.cd_expense_analysis),
                    tint = HPBlack
                )
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}

@Composable
private fun CalendarViewModeToggle(
    viewMode: ExpenseCalendarViewMode,
    onViewModeChange: (ExpenseCalendarViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = 10.dp)
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
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Column(
        modifier = modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HPWhite)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(totalLabel, style = MaterialTheme.typography.bodySmall, color = HPText)
        Spacer(modifier = Modifier.height(6.dp))
        if (isLoading) {
            CircularProgressIndicator(color = HPMain, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.height(4.dp))
        } else {
            Text(
                stringResource(R.string.expensedetail_amount_won_format, formatWon(totalAmount)),
                style = MaterialTheme.typography.titleSmall,
                color = HPSub
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(R.string.expensedetail_calendar_daily_average_format, formatWon(dailyAverage)),
                style = MaterialTheme.typography.bodySmall,
                color = HPText
            )
        }
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
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 7.sp, maxFontSize = 12.sp)
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
            color = HPSub
        )
    }
}

@Composable
private fun ExpenseCalendarListItem(record: ExpenseRecord, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val categoryLabel = resolveCategoryLabelOrNull(record.categoryId, record.customCategoryName)
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
            if (record.expenseName != null) {
                Text(
                    record.expenseName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = HPBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (categoryLabel != null) {
                Text(
                    categoryLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = HPText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        ReasonTagAndAmountColumn(
            reasonTag = reasonLabel,
            amountText = stringResource(R.string.expensedetail_amount_won_format, formatWon(record.amount))
        )
    }
}

private val PreviewCalendarReferenceToday: LocalDate = LocalDate.of(2026, 5, 20)

private fun previewCalendarRecords(referenceToday: LocalDate): Map<String, ExpenseRecord> = listOf(
    ExpenseRecord(
        id = "cal1",
        date = referenceToday,
        amount = 12_000,
        categoryId = "delivery",
        expenseName = "배달의민족",
        reasonId = "stress"
    ),
    ExpenseRecord(
        id = "cal2",
        date = referenceToday,
        amount = 4_500,
        categoryId = "cafe",
        expenseName = "스타벅스"
    ),
    ExpenseRecord(
        id = "cal3",
        date = referenceToday.minusDays(3),
        amount = 9_000,
        categoryId = "convenience",
        expenseName = "CU"
    )
).associateBy { it.id }

private fun previewMonthSummary(referenceToday: LocalDate): ExpensePeriodSummary {
    val monthStart = referenceToday.withDayOfMonth(1)
    val monthEnd = YearMonth.from(referenceToday).atEndOfMonth()
    return ExpensePeriodSummary(
        periodStart = monthStart,
        periodEnd = monthEnd,
        totalAmount = 185_000,
        dailyAverage = 12_300,
        dailyBreakdown = listOf(
            DailyAmount(referenceToday, 16_500),
            DailyAmount(referenceToday.minusDays(3), 9_000),
            DailyAmount(referenceToday.minusDays(6), 22_000)
        )
    )
}

private fun previewWeekSummary(referenceToday: LocalDate): ExpensePeriodSummary {
    val weekStart = weekGridStart(referenceToday)
    return ExpensePeriodSummary(
        periodStart = weekStart,
        periodEnd = weekStart.plusDays(6),
        totalAmount = 45_000,
        dailyAverage = 6_400,
        dailyBreakdown = listOf(
            DailyAmount(referenceToday, 16_500),
            DailyAmount(referenceToday.minusDays(3), 9_000)
        )
    )
}

@Preview(showBackground = true, name = "8. 지출 캘린더 (월간)")
@Composable
private fun ExpenseCalendarMonthlyPreview() {
    val referenceToday = PreviewCalendarReferenceToday
    HampouchTheme {
        ExpenseCalendarContent(
            referenceToday = referenceToday,
            challengeState = ChallengeState(),
            effectiveChallengePeriod = null,
            restrictToChallengePeriod = false,
            viewMode = ExpenseCalendarViewMode.MONTHLY,
            onViewModeChange = {},
            selectedDate = referenceToday,
            onDateSelected = {},
            displayedMonth = referenceToday.withDayOfMonth(1),
            onPreviousMonth = {},
            onNextMonth = {},
            displayedWeekStart = weekGridStart(referenceToday),
            onPreviousWeek = {},
            onNextWeek = {},
            records = previewCalendarRecords(referenceToday),
            monthSummary = previewMonthSummary(referenceToday),
            weekSummary = previewWeekSummary(referenceToday),
            monthLoadState = LoadState.Content(isEmpty = false),
            weekLoadState = LoadState.Content(isEmpty = false),
            dayLoadState = LoadState.Content(isEmpty = false),
            isResting = false,
            onBackClick = {},
            onExpenseClick = {},
            onExpenseAnalysisClick = {},
            onAddExpenseClick = {},
            onRetryMonth = {},
            onRetryWeek = {},
            onRetryDay = {}
        )
    }
}

@Preview(showBackground = true, name = "9. 지출 캘린더 (지출 없는 날)")
@Composable
private fun ExpenseCalendarEmptyDayPreview() {
    val referenceToday = PreviewCalendarReferenceToday
    val emptyDay = referenceToday.minusDays(1)
    HampouchTheme {
        ExpenseCalendarContent(
            referenceToday = referenceToday,
            challengeState = ChallengeState(),
            effectiveChallengePeriod = null,
            restrictToChallengePeriod = false,
            viewMode = ExpenseCalendarViewMode.MONTHLY,
            onViewModeChange = {},
            selectedDate = emptyDay,
            onDateSelected = {},
            displayedMonth = referenceToday.withDayOfMonth(1),
            onPreviousMonth = {},
            onNextMonth = {},
            displayedWeekStart = weekGridStart(referenceToday),
            onPreviousWeek = {},
            onNextWeek = {},
            records = previewCalendarRecords(referenceToday),
            monthSummary = previewMonthSummary(referenceToday),
            weekSummary = previewWeekSummary(referenceToday),
            monthLoadState = LoadState.Content(isEmpty = false),
            weekLoadState = LoadState.Content(isEmpty = false),
            dayLoadState = LoadState.Content(isEmpty = true),
            isResting = false,
            onBackClick = {},
            onExpenseClick = {},
            onExpenseAnalysisClick = {},
            onAddExpenseClick = {},
            onRetryMonth = {},
            onRetryWeek = {},
            onRetryDay = {}
        )
    }
}

@Preview(showBackground = true, name = "10. 지출 캘린더 (챌린지 종료)")
@Composable
private fun ExpenseCalendarChallengeEndedPreview() {
    val referenceToday = PreviewCalendarReferenceToday
    val challengePeriod = ExpenseDetailMockData.endedChallengePeriod(referenceToday)
    HampouchTheme {
        ExpenseCalendarContent(
            referenceToday = referenceToday,
            challengeState = ChallengeState(),
            effectiveChallengePeriod = challengePeriod,
            restrictToChallengePeriod = false,
            viewMode = ExpenseCalendarViewMode.MONTHLY,
            onViewModeChange = {},
            selectedDate = referenceToday,
            onDateSelected = {},
            displayedMonth = referenceToday.withDayOfMonth(1),
            onPreviousMonth = {},
            onNextMonth = {},
            displayedWeekStart = weekGridStart(referenceToday),
            onPreviousWeek = {},
            onNextWeek = {},
            records = previewCalendarRecords(referenceToday),
            monthSummary = previewMonthSummary(referenceToday),
            weekSummary = previewWeekSummary(referenceToday),
            monthLoadState = LoadState.Content(isEmpty = false),
            weekLoadState = LoadState.Content(isEmpty = false),
            dayLoadState = LoadState.Content(isEmpty = false),
            isResting = false,
            onBackClick = {},
            onExpenseClick = {},
            onExpenseAnalysisClick = {},
            onAddExpenseClick = {},
            onRetryMonth = {},
            onRetryWeek = {},
            onRetryDay = {}
        )
    }
}

@Preview(showBackground = true, name = "11. 지출 캘린더 (주간)")
@Composable
private fun ExpenseCalendarWeeklyPreview() {
    val referenceToday = PreviewCalendarReferenceToday
    HampouchTheme {
        ExpenseCalendarContent(
            referenceToday = referenceToday,
            challengeState = ChallengeState(),
            effectiveChallengePeriod = null,
            restrictToChallengePeriod = false,
            viewMode = ExpenseCalendarViewMode.WEEKLY,
            onViewModeChange = {},
            selectedDate = referenceToday,
            onDateSelected = {},
            displayedMonth = referenceToday.withDayOfMonth(1),
            onPreviousMonth = {},
            onNextMonth = {},
            displayedWeekStart = weekGridStart(referenceToday),
            onPreviousWeek = {},
            onNextWeek = {},
            records = previewCalendarRecords(referenceToday),
            monthSummary = previewMonthSummary(referenceToday),
            weekSummary = previewWeekSummary(referenceToday),
            monthLoadState = LoadState.Content(isEmpty = false),
            weekLoadState = LoadState.Content(isEmpty = false),
            dayLoadState = LoadState.Content(isEmpty = false),
            isResting = false,
            onBackClick = {},
            onExpenseClick = {},
            onExpenseAnalysisClick = {},
            onAddExpenseClick = {},
            onRetryMonth = {},
            onRetryWeek = {},
            onRetryDay = {}
        )
    }
}

@Preview(showBackground = true, name = "12. 지출 캘린더 (챌린지 종료 후 수정 - 제한 모드)")
@Composable
private fun ExpenseCalendarChallengeEndEditPreview() {
    val referenceToday = PreviewCalendarReferenceToday
    val challengePeriod = ExpenseDetailMockData.endedChallengePeriod(referenceToday)
    HampouchTheme {
        ExpenseCalendarContent(
            referenceToday = referenceToday,
            challengeState = ChallengeState(),
            effectiveChallengePeriod = challengePeriod,
            restrictToChallengePeriod = true,
            viewMode = ExpenseCalendarViewMode.MONTHLY,
            onViewModeChange = {},
            selectedDate = challengePeriod.endDate,
            onDateSelected = {},
            displayedMonth = challengePeriod.endDate.withDayOfMonth(1),
            onPreviousMonth = {},
            onNextMonth = {},
            displayedWeekStart = weekGridStart(referenceToday),
            onPreviousWeek = {},
            onNextWeek = {},
            records = previewCalendarRecords(referenceToday),
            monthSummary = previewMonthSummary(referenceToday),
            weekSummary = previewWeekSummary(referenceToday),
            monthLoadState = LoadState.Content(isEmpty = false),
            weekLoadState = LoadState.Content(isEmpty = false),
            dayLoadState = LoadState.Content(isEmpty = false),
            isResting = false,
            onBackClick = {},
            onExpenseClick = {},
            onExpenseAnalysisClick = {},
            onAddExpenseClick = {},
            onRetryMonth = {},
            onRetryWeek = {},
            onRetryDay = {}
        )
    }
}

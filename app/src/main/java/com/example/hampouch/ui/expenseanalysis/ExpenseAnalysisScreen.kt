package com.example.hampouch.ui.expenseanalysis

import com.example.hampouch.domain.model.AmountBreakdownItem
import com.example.hampouch.domain.model.ExpenseAnalysisEtcId
import com.example.hampouch.domain.model.ExpenseAnalysisSummary
import com.example.hampouch.domain.model.ExpenseTagAnalysisResult
import com.example.hampouch.domain.model.ExpenseTrendResult
import com.example.hampouch.domain.model.MonthlyTotal
import com.example.hampouch.domain.model.WeekdayAmount

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.R
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hampouch.core.config.ExpenseConfig
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.ui.common.FullScreenLoadError
import com.example.hampouch.ui.common.FullScreenLoadingIndicator
import com.example.hampouch.ui.common.LoadState
import com.example.hampouch.ui.common.StaleDataRefreshBanner
import com.example.hampouch.ui.expensedetail.formatWon
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPStatusSuccessBg
import com.example.hampouch.ui.theme.HPStatusSuccessText
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

sealed class ExpenseAnalysisHeaderMode {
    data class Month(val initialMonth: YearMonth) : ExpenseAnalysisHeaderMode()
    data class Challenge(val totalDays: Int, val periodStart: LocalDate, val periodEnd: LocalDate) : ExpenseAnalysisHeaderMode()
}

private val YearMonthSaver: Saver<YearMonth, Long> = Saver(
    save = { it.year * 100L + it.monthValue },
    restore = { YearMonth.of((it / 100).toInt(), (it % 100).toInt()) }
)

@Composable
fun ExpenseAnalysisRoute(
    headerMode: ExpenseAnalysisHeaderMode,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMonthlyViewClick: () -> Unit,
    onCategoryDetailClick: (LocalDate, LocalDate) -> Unit,
    onReasonDetailClick: (LocalDate, LocalDate) -> Unit,
    referenceToday: LocalDate = LocalDate.now(),
    viewModel: ExpenseAnalysisViewModel = hiltViewModel(),
    allRecords: List<ExpenseRecord> = viewModel.records.collectAsStateWithLifecycle().value
) {
    var displayedMonth by rememberSaveable(stateSaver = YearMonthSaver) {
        mutableStateOf((headerMode as? ExpenseAnalysisHeaderMode.Month)?.initialMonth ?: YearMonth.from(referenceToday))
    }

    val periodStart: LocalDate
    val periodEnd: LocalDate
    when (headerMode) {
        is ExpenseAnalysisHeaderMode.Month -> {
            periodStart = displayedMonth.atDay(1)
            periodEnd = displayedMonth.atEndOfMonth()
        }
        is ExpenseAnalysisHeaderMode.Challenge -> {
            periodStart = headerMode.periodStart
            periodEnd = headerMode.periodEnd
        }
    }

    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val loadState by viewModel.loadState.collectAsStateWithLifecycle()
    LaunchedEffect(periodStart, periodEnd) { viewModel.loadSummary(periodStart, periodEnd) }

    ExpenseAnalysisContent(
        headerMode = headerMode,
        displayedMonth = displayedMonth,
        onPreviousMonth = { displayedMonth = displayedMonth.minusMonths(1) },
        onNextMonth = { displayedMonth = displayedMonth.plusMonths(1) },
        summary = summary,
        loadState = loadState,
        onBackClick = onBackClick,
        modifier = modifier,
        onMonthlyViewClick = onMonthlyViewClick,
        onCategoryDetailClick = onCategoryDetailClick,
        onReasonDetailClick = onReasonDetailClick,
        onRetry = viewModel::retry
    )
}

@Composable
private fun ExpenseAnalysisContent(
    headerMode: ExpenseAnalysisHeaderMode,
    displayedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    summary: ExpenseAnalysisSummary?,
    loadState: LoadState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMonthlyViewClick: () -> Unit,
    onCategoryDetailClick: (LocalDate, LocalDate) -> Unit,
    onReasonDetailClick: (LocalDate, LocalDate) -> Unit,
    onRetry: () -> Unit
) {
    val periodStart: LocalDate
    val periodEnd: LocalDate
    when (headerMode) {
        is ExpenseAnalysisHeaderMode.Month -> {
            periodStart = displayedMonth.atDay(1)
            periodEnd = displayedMonth.atEndOfMonth()
        }
        is ExpenseAnalysisHeaderMode.Challenge -> {
            periodStart = headerMode.periodStart
            periodEnd = headerMode.periodEnd
        }
    }

    val totalAmount = summary?.totalAmount ?: 0
    val categoryItems = summary?.categoryBreakdown.orEmpty()
    val reasonItemsSorted = summary?.reasonBreakdown?.sortedByDescending { it.amount }.orEmpty()
    val weekdayItems = summary?.weekdayBreakdown
        ?: ExpenseAnalysisWeekdayOrder.map { WeekdayAmount(it, 0) }
    val peakDays = remember(weekdayItems) {
        val maxAmount = weekdayItems.maxOfOrNull { it.amount } ?: 0
        if (maxAmount <= 0) emptySet() else weekdayItems.filter { it.amount == maxAmount }.map { it.dayOfWeek }.take(2).toSet()
    }

    val topCategoryAmount = categoryItems.maxOfOrNull { it.amount } ?: 0
    val topCategoryItems = categoryItems.filter { it.amount == topCategoryAmount && topCategoryAmount > 0 }.take(2)
    val topReasonItem = reasonItemsSorted.firstOrNull()
    val peakWeekdayNames = weekdayFullNames(ExpenseAnalysisWeekdayOrder.filter { it in peakDays }.take(2))
    val aiPeriodLabel = when (headerMode) {
        is ExpenseAnalysisHeaderMode.Month -> stringResource(R.string.expenseanalysis_monthly_month_format, displayedMonth.monthValue)
        is ExpenseAnalysisHeaderMode.Challenge -> stringResource(R.string.expenseanalysis_challenge_title_format, headerMode.totalDays)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            ExpenseAnalysisTopBar(title = stringResource(R.string.expenseanalysis_title), onBackClick = onBackClick)
        },
        containerColor = HPGray2
    ) { innerPadding ->
        if (loadState is LoadState.Failure && summary == null) {
            FullScreenLoadError(
                message = (loadState as LoadState.Failure).message,
                onRetry = onRetry,
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }
        if (summary == null && loadState !is LoadState.Content) {
            FullScreenLoadingIndicator(modifier = Modifier.padding(innerPadding))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 15.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            if (loadState is LoadState.Failure && summary != null) {
                StaleDataRefreshBanner(
                    message = (loadState as LoadState.Failure).message,
                    onRetry = onRetry
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (headerMode is ExpenseAnalysisHeaderMode.Month) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        stringResource(R.string.expenseanalysis_monthly_view_link),
                        style = MaterialTheme.typography.bodySmall,
                        color = HPText,
                        modifier = Modifier.clickable(onClick = onMonthlyViewClick)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                MonthHeaderCard(
                    month = displayedMonth,
                    totalAmount = totalAmount,
                    onPrevious = onPreviousMonth,
                    onNext = onNextMonth
                )
            } else if (headerMode is ExpenseAnalysisHeaderMode.Challenge) {
                ChallengeHeaderCard(
                    totalDays = headerMode.totalDays,
                    periodStart = periodStart,
                    periodEnd = periodEnd,
                    totalAmount = totalAmount
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            AnalysisSectionHeader(
                title = stringResource(R.string.expenseanalysis_category_section_title),
                onDetailClick = { onCategoryDetailClick(periodStart, periodEnd) }
            )
            Spacer(modifier = Modifier.height(10.dp))
            CategoryAnalysisCard(items = categoryItems, totalAmount = totalAmount)

            Spacer(modifier = Modifier.height(20.dp))
            AnalysisSectionHeader(
                title = stringResource(R.string.expenseanalysis_reason_section_title),
                onDetailClick = { onReasonDetailClick(periodStart, periodEnd) }
            )
            Spacer(modifier = Modifier.height(10.dp))
            ReasonAnalysisCard(items = reasonItemsSorted)

            Spacer(modifier = Modifier.height(20.dp))
            WeekdayAnalysisCard(items = weekdayItems, peakDays = peakDays)

            Spacer(modifier = Modifier.height(20.dp))
            PochiAnalysisCard(
                periodLabel = aiPeriodLabel,
                totalAmount = totalAmount,
                topCategoryLabels = topCategoryItems.map { analysisCategoryLabel(it.id) }.joinToString(", "),
                topCategoryPercent = topCategoryItems.firstOrNull()?.percent ?: 0,
                topReasonLabel = topReasonItem?.let { analysisReasonTabLabel(it.id) }.orEmpty(),
                topReasonPercent = topReasonItem?.percent ?: 0,
                peakWeekdayLabels = peakWeekdayNames.joinToString(", ").ifEmpty { null },
                serverInsight = summary?.pouchInsight
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseAnalysisTopBar(title: String, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = { Text(title, style = MaterialTheme.typography.titleSmall, color = HPBlack) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_expense_analysis_back),
                    tint = HPBlack
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = HPGray2)
    )
}

@Composable
private fun MonthHeaderCard(
    month: YearMonth,
    totalAmount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(HPSub4)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.cd_calendar_month_prev), tint = HPBlack)
            }
            Text(monthYearLabel(month), style = MaterialTheme.typography.titleSmall, color = HPSub)
            IconButton(onClick = onNext) {
                Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.cd_calendar_month_next), tint = HPBlack)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.expensedetail_amount_won_format, formatWon(totalAmount)),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = HPBlack
        )
    }
}

@Composable
private fun ChallengeHeaderCard(
    totalDays: Int,
    periodStart: LocalDate,
    periodEnd: LocalDate,
    totalAmount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(HPSub4)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.expenseanalysis_challenge_title_format, totalDays),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = HPMain
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(
                R.string.expenseanalysis_period_range_format,
                periodStart.monthValue, periodStart.dayOfMonth, periodEnd.monthValue, periodEnd.dayOfMonth
            ),
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.expensedetail_amount_won_format, formatWon(totalAmount)),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = HPBlack
        )
    }
}

@Composable
private fun AnalysisSectionHeader(title: String, onDetailClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = Body16Bold, color = HPBlack, modifier = Modifier.weight(1f))
        Text(
            stringResource(R.string.expenseanalysis_detail_view_link),
            style = MaterialTheme.typography.bodySmall,
            color = HPText,
            modifier = Modifier.clickable(onClick = onDetailClick)
        )
    }
}

@Composable
private fun CategoryAnalysisCard(items: List<AmountBreakdownItem>, totalAmount: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite)
            .padding(5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ExpenseAnalysisDonutChart(
                items = items,
                totalAmount = totalAmount,
                colorOf = { id -> analysisCategoryColor(id) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1.7f)) {
                items.forEach { item ->
                    CategoryLegendRow(
                        label = analysisCategoryLabel(item.id),
                        dotColor = analysisCategoryColor(item.id),
                        amount = item.amount,
                        percent = item.percent
                    )
                }
            }
        }
    }
}

@Composable
private fun ReasonAnalysisCard(items: List<AmountBreakdownItem>, modifier: Modifier = Modifier) {
    val maxAmount = (items.maxOfOrNull { it.amount } ?: 0).coerceAtLeast(1)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite)
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { item ->
                ReasonStatChip(item = item, modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.forEach { item ->
                ReasonProgressRow(
                    label = analysisReasonShortLabel(item.id),
                    amount = item.amount,
                    fraction = item.amount / maxAmount.toFloat(),
                    barColor = analysisReasonBarColor(item.id)
                )
            }
        }
    }
}

@Composable
private fun WeekdayAnalysisCard(items: List<WeekdayAmount>, peakDays: Set<DayOfWeek>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite)
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.expenseanalysis_weekday_section_title),
                style = Body16Bold,
                color = HPBlack,
                modifier = Modifier.weight(1f)
            )
            val peakLabel = weekdayPeakLabel(peakDays)
            if (peakLabel != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(HPSub4)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(peakLabel, style = MaterialTheme.typography.labelMedium, color = HPMain)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        WeekdayBarChart(items = items, peakDays = peakDays)
    }
}

@Composable
private fun weekdayPeakLabel(peakDays: Set<DayOfWeek>): String? {
    if (peakDays.isEmpty()) return null
    val ordered = ExpenseAnalysisWeekdayOrder.filter { it in peakDays }
    val names = ordered.map { it.getDisplayName(TextStyle.NARROW, Locale.KOREA) + stringResource(R.string.expenseanalysis_weekday_suffix) }
    return if (names.size >= 2) {
        stringResource(R.string.expenseanalysis_weekday_peak_two_format, names[0], names[1])
    } else {
        stringResource(R.string.expenseanalysis_weekday_peak_one_format, names[0])
    }
}

@Composable
fun MonthlyExpenseRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    referenceToday: LocalDate = LocalDate.now(),
    viewModel: ExpenseAnalysisViewModel = hiltViewModel(),
    allRecords: List<ExpenseRecord> = viewModel.records.collectAsStateWithLifecycle().value
) {
    val currentMonth = YearMonth.from(referenceToday)
    val trend by viewModel.trend.collectAsStateWithLifecycle()
    val loadState by viewModel.loadState.collectAsStateWithLifecycle()
    LaunchedEffect(currentMonth) { viewModel.loadTrend(currentMonth) }

    MonthlyExpenseContent(
        currentMonth = currentMonth,
        trend = trend,
        loadState = loadState,
        onBackClick = onBackClick,
        modifier = modifier,
        onRetry = viewModel::retry
    )
}

@Composable
private fun MonthlyExpenseContent(
    currentMonth: YearMonth,
    trend: ExpenseTrendResult?,
    loadState: LoadState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    val totals = trend?.trend.orEmpty()
    val currentTotal = trend?.totalAmount ?: 0
    val average = trend?.monthlyAverage ?: 0
    val changePercent = trend?.diffRateFromLastMonth ?: 0
    val maxTotal = totals.maxOfOrNull { it.amount } ?: 0

    Scaffold(
        modifier = modifier,
        topBar = { DetailTopBar(title = stringResource(R.string.expenseanalysis_monthly_title), onBackClick = onBackClick) },
        containerColor = HPGray2
    ) { innerPadding ->
        if (loadState is LoadState.Failure && trend == null) {
            FullScreenLoadError(
                message = (loadState as LoadState.Failure).message,
                onRetry = onRetry,
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }
        if (trend == null && loadState !is LoadState.Content) {
            FullScreenLoadingIndicator(modifier = Modifier.padding(innerPadding))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 15.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            if (loadState is LoadState.Failure && trend != null) {
                StaleDataRefreshBanner(
                    message = (loadState as LoadState.Failure).message,
                    onRetry = onRetry
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Text(
                stringResource(R.string.expenseanalysis_monthly_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = HPText
            )
            Spacer(modifier = Modifier.height(16.dp))
            MonthlyStatRow(currentMonth = currentMonth, currentTotal = currentTotal, average = average, changePercent = changePercent)
            Spacer(modifier = Modifier.height(20.dp))
            MonthlyTrendChart(items = totals)
            Spacer(modifier = Modifier.height(20.dp))
            Text(stringResource(R.string.expenseanalysis_monthly_list_title), style = Body16Bold, color = HPBlack)
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                totals.forEach { total ->
                    MonthlyTotalRow(
                        total = total,
                        isHighest = maxTotal > 0 && total.amount == maxTotal,
                        isCurrent = total.month == currentMonth,
                        maxAmount = maxTotal
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MonthlyStatRow(
    currentMonth: YearMonth,
    currentTotal: Int,
    average: Int,
    changePercent: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub4)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MonthlyStatItem(
            label = stringResource(R.string.expenseanalysis_monthly_stat_this_month_format, currentMonth.monthValue),
            value = stringResource(R.string.expensedetail_amount_won_format, formatWon(currentTotal)),
            valueColor = HPMain
        )
        MonthlyStatItem(
            label = stringResource(R.string.expenseanalysis_monthly_stat_average_label),
            value = stringResource(R.string.expensedetail_amount_won_format, formatWon(average))
        )
        MonthlyStatItem(
            label = stringResource(R.string.expenseanalysis_monthly_stat_change_label),
            value = when {
                changePercent > 0 -> stringResource(R.string.expenseanalysis_monthly_change_increase_format, changePercent)
                changePercent < 0 -> stringResource(R.string.expenseanalysis_monthly_change_decrease_format, -changePercent)
                else -> stringResource(R.string.expenseanalysis_monthly_change_none)
            },
            valueColor = HPMain
        )
    }
}

@Composable
private fun MonthlyStatItem(label: String, value: String, valueColor: Color = HPBlack, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = HPText)
        Spacer(modifier = Modifier.height(6.dp))
        Text(value, style = Body16Bold, color = valueColor)
    }
}

@Composable
private fun MonthlyTotalRow(
    total: MonthlyTotal,
    isHighest: Boolean,
    isCurrent: Boolean,
    maxAmount: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier.width(88.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.expenseanalysis_monthly_month_format, total.month.monthValue),
                style = MaterialTheme.typography.bodySmall,
                color = HPBlack
            )
            if (isHighest || isCurrent) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isHighest) HPStatusSuccessBg else HPSub3)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        stringResource(
                            if (isHighest) R.string.expenseanalysis_monthly_badge_highest else R.string.expenseanalysis_monthly_badge_current
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isHighest) HPStatusSuccessText else HPMain,
                        fontSize = 10.sp
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(end = 20.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(HPGray4)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (total.amount / maxAmount.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(50))
                    .background(if (isHighest) HPSub else if (isCurrent) HPSub1 else HPSub2)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            stringResource(R.string.expensedetail_amount_won_format, formatWon(total.amount)),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = HPBlack
        )
    }
}

@Composable
fun CategoryDetailRoute(
    periodStart: LocalDate,
    periodEnd: LocalDate,
    initialCategoryId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    referenceToday: LocalDate = LocalDate.now(),
    viewModel: ExpenseAnalysisViewModel = hiltViewModel(),
    allRecords: List<ExpenseRecord> = viewModel.records.collectAsStateWithLifecycle().value
) {
    var selectedId by remember { mutableStateOf(initialCategoryId) }
    val tagResult by viewModel.tagResult.collectAsStateWithLifecycle()
    val loadState by viewModel.loadState.collectAsStateWithLifecycle()
    LaunchedEffect(selectedId, periodStart, periodEnd) {
        viewModel.loadCategoryAnalysis(selectedId, periodStart, periodEnd)
    }

    CategoryDetailContent(
        selectedId = selectedId,
        onSelectId = { selectedId = it },
        tagResult = tagResult,
        loadState = loadState,
        onBackClick = onBackClick,
        modifier = modifier,
        onRetry = viewModel::retry
    )
}

@Composable
private fun CategoryDetailContent(
    selectedId: String,
    onSelectId: (String) -> Unit,
    tagResult: ExpenseTagAnalysisResult?,
    loadState: LoadState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    val selectedAmount = tagResult?.totalAmount ?: 0
    val selectedPercent = tagResult?.percent ?: 0
    val records = tagResult?.records.orEmpty()

    Scaffold(
        modifier = modifier,
        topBar = { DetailTopBar(title = stringResource(R.string.expenseanalysis_category_section_title), onBackClick = onBackClick) },
        containerColor = HPGray2
    ) { innerPadding ->
        if (loadState is LoadState.Failure && tagResult == null) {
            FullScreenLoadError(
                message = (loadState as LoadState.Failure).message,
                onRetry = onRetry,
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }
        if (tagResult == null && loadState !is LoadState.Content) {
            FullScreenLoadingIndicator(modifier = Modifier.padding(innerPadding))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 15.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            if (loadState is LoadState.Failure && tagResult != null) {
                StaleDataRefreshBanner(
                    message = (loadState as LoadState.Failure).message,
                    onRetry = onRetry
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            AnalysisTabRow(
                tabs = ExpenseAnalysisCategoryTabOrder.map { it to analysisCategoryLabel(it) },
                selectedId = selectedId,
                onSelect = onSelectId
            )
            Spacer(modifier = Modifier.height(16.dp))
            DetailSpentSummaryCard(
                titleFormatResId = R.string.expenseanalysis_category_detail_spent_format,
                label = analysisCategoryLabel(selectedId),
                amount = selectedAmount,
                count = records.size,
                percent = selectedPercent
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(records, key = { it.id }, contentType = { "expense_record" }) { record ->
                    ExpenseAnalysisListItem(record = record, showReasonChip = true, onClick = {})
                }
            }
        }
    }
}

@Composable
fun ReasonDetailRoute(
    periodStart: LocalDate,
    periodEnd: LocalDate,
    initialReasonId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    referenceToday: LocalDate = LocalDate.now(),
    viewModel: ExpenseAnalysisViewModel = hiltViewModel(),
    allRecords: List<ExpenseRecord> = viewModel.records.collectAsStateWithLifecycle().value
) {
    var selectedId by remember { mutableStateOf(initialReasonId) }
    val tagResult by viewModel.tagResult.collectAsStateWithLifecycle()
    val loadState by viewModel.loadState.collectAsStateWithLifecycle()
    LaunchedEffect(selectedId, periodStart, periodEnd) {
        viewModel.loadEmotionAnalysis(selectedId, periodStart, periodEnd)
    }

    ReasonDetailContent(
        selectedId = selectedId,
        onSelectId = { selectedId = it },
        tagResult = tagResult,
        loadState = loadState,
        onBackClick = onBackClick,
        modifier = modifier,
        onRetry = viewModel::retry
    )
}

@Composable
private fun ReasonDetailContent(
    selectedId: String,
    onSelectId: (String) -> Unit,
    tagResult: ExpenseTagAnalysisResult?,
    loadState: LoadState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    val selectedAmount = tagResult?.totalAmount ?: 0
    val selectedPercent = tagResult?.percent ?: 0
    val records = tagResult?.records.orEmpty()

    Scaffold(
        modifier = modifier,
        topBar = { DetailTopBar(title = stringResource(R.string.expenseanalysis_reason_section_title), onBackClick = onBackClick) },
        containerColor = HPGray2
    ) { innerPadding ->
        if (loadState is LoadState.Failure && tagResult == null) {
            FullScreenLoadError(
                message = (loadState as LoadState.Failure).message,
                onRetry = onRetry,
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }
        if (tagResult == null && loadState !is LoadState.Content) {
            FullScreenLoadingIndicator(modifier = Modifier.padding(innerPadding))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 15.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            if (loadState is LoadState.Failure && tagResult != null) {
                StaleDataRefreshBanner(
                    message = (loadState as LoadState.Failure).message,
                    onRetry = onRetry
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            AnalysisTabRow(
                tabs = ExpenseAnalysisReasonTabOrder.map { it to analysisReasonTabLabel(it) },
                selectedId = selectedId,
                onSelect = onSelectId
            )
            Spacer(modifier = Modifier.height(16.dp))
            DetailSpentSummaryCard(
                titleFormatResId = R.string.expenseanalysis_reason_detail_spent_format,
                label = analysisReasonTabLabel(selectedId),
                amount = selectedAmount,
                count = records.size,
                percent = selectedPercent
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(records, key = { it.id }, contentType = { "expense_record" }) { record ->
                    ExpenseAnalysisListItem(record = record, showReasonChip = true, onClick = {})
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailTopBar(title: String, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = { Text(title, style = MaterialTheme.typography.titleSmall, color = HPBlack) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_expense_analysis_back),
                    tint = HPBlack
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = HPGray2)
    )
}

private fun previewExpenseAnalysisSummary(): ExpenseAnalysisSummary = ExpenseAnalysisSummary(
    periodStart = LocalDate.of(2026, 5, 1),
    periodEnd = LocalDate.of(2026, 5, 31),
    totalAmount = 312_000,
    categoryBreakdown = listOf(
        AmountBreakdownItem(id = "delivery", amount = 120_000, percent = 38),
        AmountBreakdownItem(id = "dining_out", amount = 80_000, percent = 26),
        AmountBreakdownItem(id = "cafe", amount = 62_000, percent = 20),
        AmountBreakdownItem(id = ExpenseAnalysisEtcId, amount = 50_000, percent = 16)
    ),
    reasonBreakdown = listOf(
        AmountBreakdownItem(id = "stress", amount = 150_000, percent = 48),
        AmountBreakdownItem(id = "reward", amount = 90_000, percent = 29),
        AmountBreakdownItem(id = "lazy", amount = 42_000, percent = 13),
        AmountBreakdownItem(id = ExpenseAnalysisEtcId, amount = 30_000, percent = 10)
    ),
    weekdayBreakdown = listOf(
        WeekdayAmount(DayOfWeek.MONDAY, 30_000),
        WeekdayAmount(DayOfWeek.TUESDAY, 25_000),
        WeekdayAmount(DayOfWeek.WEDNESDAY, 40_000),
        WeekdayAmount(DayOfWeek.THURSDAY, 35_000),
        WeekdayAmount(DayOfWeek.FRIDAY, 62_000),
        WeekdayAmount(DayOfWeek.SATURDAY, 62_000),
        WeekdayAmount(DayOfWeek.SUNDAY, 58_000)
    ),
    weekdayInsight = null,
    pouchInsight = "이번 달은 배달 지출이 가장 컸어요. 스트레스로 인한 지출이 많으니 다른 방식으로 풀어보는 건 어떨까요?"
)

private fun previewExpenseAnalysisRecords(): List<ExpenseRecord> = listOf(
    ExpenseRecord(
        id = "preview1",
        date = LocalDate.of(2026, 5, 20),
        amount = 20_000,
        categoryId = "delivery",
        expenseName = "배달의민족",
        reasonId = "stress"
    ),
    ExpenseRecord(
        id = "preview2",
        date = LocalDate.of(2026, 5, 18),
        amount = 15_000,
        categoryId = "dining_out",
        expenseName = "한신 포차",
        reasonId = "reward"
    ),
    ExpenseRecord(
        id = "preview3",
        date = LocalDate.of(2026, 5, 12),
        amount = 8_000,
        customCategoryName = "자취 생활비",
        reasonId = "lazy"
    )
)

private fun previewExpenseTrendResult(): ExpenseTrendResult = ExpenseTrendResult(
    month = YearMonth.of(2026, 5),
    totalAmount = 312_000,
    monthlyAverage = 280_000,
    diffRateFromLastMonth = 12,
    trend = listOf(
        MonthlyTotal(YearMonth.of(2025, 12), 210_000),
        MonthlyTotal(YearMonth.of(2026, 1), 260_000),
        MonthlyTotal(YearMonth.of(2026, 2), 240_000),
        MonthlyTotal(YearMonth.of(2026, 3), 290_000),
        MonthlyTotal(YearMonth.of(2026, 4), 278_000),
        MonthlyTotal(YearMonth.of(2026, 5), 312_000)
    ),
    trendInsight = null
)

private fun previewExpenseTagResult(id: String): ExpenseTagAnalysisResult = ExpenseTagAnalysisResult(
    id = id,
    totalAmount = 120_000,
    count = 3,
    percent = 38,
    records = previewExpenseAnalysisRecords()
)

@Preview(showBackground = true, name = "1. 식비 지출 분석 (월별)")
@Composable
private fun ExpenseAnalysisMonthPreview() {
    HampouchTheme {
        ExpenseAnalysisContent(
            headerMode = ExpenseAnalysisHeaderMode.Month(YearMonth.of(2026, 5)),
            displayedMonth = YearMonth.of(2026, 5),
            onPreviousMonth = {},
            onNextMonth = {},
            summary = previewExpenseAnalysisSummary(),
            loadState = LoadState.Content(isEmpty = false),
            onBackClick = {},
            onMonthlyViewClick = {},
            onCategoryDetailClick = { _, _ -> },
            onReasonDetailClick = { _, _ -> },
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "7. 식비 지출 분석 (챌린지 기간)")
@Composable
private fun ExpenseAnalysisChallengePreview() {
    HampouchTheme {
        ExpenseAnalysisContent(
            headerMode = ExpenseAnalysisHeaderMode.Challenge(
                totalDays = 14,
                periodStart = LocalDate.of(2026, 5, 1),
                periodEnd = LocalDate.of(2026, 5, 14)
            ),
            displayedMonth = YearMonth.of(2026, 5),
            onPreviousMonth = {},
            onNextMonth = {},
            summary = previewExpenseAnalysisSummary(),
            loadState = LoadState.Content(isEmpty = false),
            onBackClick = {},
            onMonthlyViewClick = {},
            onCategoryDetailClick = { _, _ -> },
            onReasonDetailClick = { _, _ -> },
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "2. 월별 지출")
@Composable
private fun MonthlyExpensePreview() {
    HampouchTheme {
        MonthlyExpenseContent(
            currentMonth = YearMonth.of(2026, 5),
            trend = previewExpenseTrendResult(),
            loadState = LoadState.Content(isEmpty = false),
            onBackClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "3. 카테고리별 지출 (배달)")
@Composable
private fun CategoryDetailDeliveryPreview() {
    HampouchTheme {
        CategoryDetailContent(
            selectedId = "delivery",
            onSelectId = {},
            tagResult = previewExpenseTagResult("delivery"),
            loadState = LoadState.Content(isEmpty = false),
            onBackClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "4. 카테고리별 지출 (기타)")
@Composable
private fun CategoryDetailEtcPreview() {
    HampouchTheme {
        CategoryDetailContent(
            selectedId = ExpenseAnalysisEtcId,
            onSelectId = {},
            tagResult = previewExpenseTagResult(ExpenseAnalysisEtcId),
            loadState = LoadState.Content(isEmpty = false),
            onBackClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "5. 지출 이유 (스트레스)")
@Composable
private fun ReasonDetailStressPreview() {
    HampouchTheme {
        ReasonDetailContent(
            selectedId = "stress",
            onSelectId = {},
            tagResult = previewExpenseTagResult("stress"),
            loadState = LoadState.Content(isEmpty = false),
            onBackClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "6. 지출 이유 (기타)")
@Composable
private fun ReasonDetailEtcPreview() {
    HampouchTheme {
        ReasonDetailContent(
            selectedId = ExpenseAnalysisEtcId,
            onSelectId = {},
            tagResult = previewExpenseTagResult(ExpenseAnalysisEtcId),
            loadState = LoadState.Content(isEmpty = false),
            onBackClick = {},
            onRetry = {}
        )
    }
}

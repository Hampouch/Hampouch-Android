package com.example.hampouch.ui.expenseanalysis

import com.example.hampouch.domain.model.AmountBreakdownItem
import com.example.hampouch.domain.model.ExpenseAnalysisEtcId
import com.example.hampouch.domain.model.MonthlyTotal
import com.example.hampouch.domain.model.WeekdayAmount

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.R
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.ui.expensedetail.DashedDivider
import com.example.hampouch.ui.expensedetail.ExpenseReasonCatalog
import com.example.hampouch.ui.expensedetail.formatWon
import com.example.hampouch.ui.expensedetail.resolveCategoryColor
import com.example.hampouch.ui.expensedetail.resolveCategoryIcon
import com.example.hampouch.ui.expensedetail.resolveReasonLabel
import com.example.hampouch.ui.home.HomeCategoryCatalog
import com.example.hampouch.ui.theme.Body16Bold
import com.example.hampouch.ui.theme.HampouchTheme
import com.example.hampouch.ui.theme.HPAnalysisCafe
import com.example.hampouch.ui.theme.HPAnalysisConvenience
import com.example.hampouch.ui.theme.HPAnalysisDelivery
import com.example.hampouch.ui.theme.HPAnalysisDiningOut
import com.example.hampouch.ui.theme.HPAnalysisDrink
import com.example.hampouch.ui.theme.HPAnalysisEtc
import com.example.hampouch.ui.theme.HPAnalysisMart
import com.example.hampouch.ui.theme.HPAnalysisSnack
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPTipEtcBg
import com.example.hampouch.ui.theme.HPWhite
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val analysisCategoryColors: Map<String, Color> = mapOf(
    "delivery" to HPAnalysisDelivery,
    "snack" to HPAnalysisSnack,
    "dining_out" to HPAnalysisDiningOut,
    "mart" to HPAnalysisMart,
    "convenience" to HPAnalysisConvenience,
    "drink" to HPAnalysisDrink,
    "cafe" to HPAnalysisCafe,
    ExpenseAnalysisEtcId to HPAnalysisEtc
)

fun analysisCategoryColor(categoryId: String): Color = analysisCategoryColors[categoryId] ?: HPAnalysisEtc

private fun withRoParticle(word: String): String {
    val lastChar = word.lastOrNull() ?: return word
    val syllableIndex = lastChar.code - 0xAC00
    if (syllableIndex !in 0..11171) return "${word}로"
    val hasFinalConsonant = syllableIndex % 28 != 0
    val isRieulFinal = syllableIndex % 28 == 8
    return if (hasFinalConsonant && !isRieulFinal) "${word}으로" else "${word}로"
}

@Composable
fun analysisCategoryLabel(categoryId: String): String =
    if (categoryId == ExpenseAnalysisEtcId) {
        stringResource(R.string.category_etc)
    } else {
        HomeCategoryCatalog.byId(categoryId)?.let { stringResource(it.labelResId) } ?: stringResource(R.string.category_etc)
    }

@Composable
fun analysisReasonTabLabel(reasonId: String): String =
    if (reasonId == ExpenseAnalysisEtcId) {
        stringResource(R.string.category_etc)
    } else {
        ExpenseReasonCatalog.byId(reasonId)?.let { stringResource(it.labelResId) }
            ?: stringResource(R.string.category_etc)
    }

@Composable
fun resolveAnalysisCategoryLabel(record: ExpenseRecord): String {
    val known = HomeCategoryCatalog.byId(record.categoryId)
    return when {
        known != null -> stringResource(known.labelResId)
        record.customCategoryName != null -> stringResource(R.string.expenseanalysis_custom_category_label)
        else -> stringResource(R.string.category_etc)
    }
}

@Composable
fun ExpenseAnalysisDonutChart(
    items: List<AmountBreakdownItem>,
    totalAmount: Int,
    colorOf: @Composable (String) -> Color,
    modifier: Modifier = Modifier
) {
    val colors = items.associate { it.id to colorOf(it.id) }
    Box(modifier = modifier.size(width = 140.dp, height = 140.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.minDimension * 0.15f
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            var startAngle = -90f
            items.filter { it.amount > 0 }.forEach { item ->
                val sweep = 360f * item.amount / totalAmount.coerceAtLeast(1)
                drawArc(
                    color = colors[item.id] ?: HPGray5,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                startAngle += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(R.string.expenseanalysis_total_spent_label),
                style = MaterialTheme.typography.labelMedium,
                color = HPText
            )
            Text(
                stringResource(R.string.expensedetail_amount_won_format, formatWon(totalAmount)),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = HPBlack,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, name = "지출 분석 도넛 차트")
@Composable
private fun ExpenseAnalysisDonutChartPreview() {
    HampouchTheme {
        ExpenseAnalysisDonutChart(
            items = listOf(
                AmountBreakdownItem(id = "delivery", amount = 40_000, percent = 40),
                AmountBreakdownItem(id = "cafe", amount = 30_000, percent = 30),
                AmountBreakdownItem(id = ExpenseAnalysisEtcId, amount = 30_000, percent = 30)
            ),
            totalAmount = 100_000,
            colorOf = { id -> analysisCategoryColor(id) }
        )
    }
}

@Composable
fun CategoryLegendRow(
    label: String,
    dotColor: Color,
    amount: Int,
    percent: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = HPBlack,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            stringResource(R.string.expensedetail_amount_won_format, formatWon(amount)),
            style = MaterialTheme.typography.bodyMedium,
            color = HPBlack,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            stringResource(R.string.expenseanalysis_percent_format, percent),
            style = MaterialTheme.typography.bodyMedium,
            color = HPText,
            textAlign = TextAlign.End,
            modifier = Modifier.width(36.dp)
        )
    }
}

@Preview(showBackground = true, name = "카테고리 범례 행")
@Composable
private fun CategoryLegendRowPreview() {
    HampouchTheme {
        CategoryLegendRow(label = "배달음식", dotColor = HPAnalysisDelivery, amount = 40_000, percent = 40)
    }
}

private data class ReasonVisual(val iconRes: Int?, val background: Color, val tint: Color, val labelResId: Int)

private fun reasonVisual(reasonId: String): ReasonVisual = when (reasonId) {
    "craving" -> ReasonVisual(R.drawable.icon_fork, Color(0x33729739), Color(0xFF729739), R.string.expenseanalysis_reason_craving_short)
    "stress" -> ReasonVisual(R.drawable.icon_stress, Color(0x33E17866), Color(0xFFE17866), R.string.expenseanalysis_reason_stress_short)
    "lazy" -> ReasonVisual(R.drawable.icon_sleepy, Color(0x3342A8A7), Color(0xFF42A8A7), R.string.expenseanalysis_reason_lazy_short)
    "reward" -> ReasonVisual(R.drawable.icon_present, Color(0x33B19347), Color(0xFFB19347), R.string.expenseanalysis_reason_reward_short)
    else -> ReasonVisual(null, HPTipEtcBg, HPBlack, R.string.expenseanalysis_reason_etc_short)
}

@Composable
fun analysisReasonShortLabel(reasonId: String): String = stringResource(reasonVisual(reasonId).labelResId)

@Composable
fun analysisReasonBarColor(reasonId: String): Color = reasonVisual(reasonId).tint

@Composable
fun ReasonStatChip(item: AmountBreakdownItem, modifier: Modifier = Modifier) {
    val visual = reasonVisual(item.id)
    Column(
        modifier = modifier
            .height(94.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(visual.background)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        if (visual.iconRes != null) {
            Image(painter = painterResource(visual.iconRes), contentDescription = null, modifier = Modifier.size(22.dp))
        } else {
            Icon(Icons.Filled.MoreHoriz, contentDescription = null, tint = visual.tint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(stringResource(R.string.expenseanalysis_percent_format, item.percent), style = Body16Bold, color = HPBlack)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            stringResource(visual.labelResId),
            style = MaterialTheme.typography.labelSmall,
            color = HPText,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true, name = "이유별 통계 칩")
@Composable
private fun ReasonStatChipPreview() {
    HampouchTheme {
        ReasonStatChip(item = AmountBreakdownItem(id = "stress", amount = 40_000, percent = 40))
    }
}

@Composable
fun ReasonProgressRow(label: String, amount: Int, fraction: Float, barColor: Color, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = HPBlack,
            modifier = Modifier.width(72.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(HPGray4)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(50))
                    .background(barColor)
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            stringResource(R.string.expensedetail_amount_won_format, formatWon(amount)),
            style = MaterialTheme.typography.bodySmall,
            color = HPBlack,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.width(76.dp)
        )
    }
}

@Preview(showBackground = true, name = "이유별 진행률 행")
@Composable
private fun ReasonProgressRowPreview() {
    HampouchTheme {
        ReasonProgressRow(label = "스트레스", amount = 40_000, fraction = 0.4f, barColor = HPSub1)
    }
}

@Composable
private fun weekdayShortLabel(dayOfWeek: DayOfWeek): String =
    dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.KOREA)

@Composable
fun WeekdayBarChart(items: List<WeekdayAmount>, peakDays: Set<DayOfWeek>, modifier: Modifier = Modifier) {
    val maxAmount = (items.maxOfOrNull { it.amount } ?: 0).coerceAtLeast(1)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().height(96.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            items.forEach { item ->
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .fillMaxHeight(fraction = (item.amount / maxAmount.toFloat()).coerceIn(0.04f, 1f))
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(if (item.dayOfWeek in peakDays) HPSub1 else HPSub2)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            items.forEach { item ->
                Text(
                    weekdayShortLabel(item.dayOfWeek),
                    style = MaterialTheme.typography.labelMedium,
                    color = HPText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "요일별 지출 막대 그래프")
@Composable
private fun WeekdayBarChartPreview() {
    HampouchTheme {
        WeekdayBarChart(
            items = listOf(
                WeekdayAmount(DayOfWeek.MONDAY, 10_000),
                WeekdayAmount(DayOfWeek.TUESDAY, 25_000),
                WeekdayAmount(DayOfWeek.WEDNESDAY, 5_000),
                WeekdayAmount(DayOfWeek.THURSDAY, 15_000)
            ),
            peakDays = setOf(DayOfWeek.TUESDAY)
        )
    }
}

private fun niceAxisStep(rawStep: Float): Float {
    if (rawStep <= 0f) return 1f
    val magnitude = Math.pow(10.0, Math.floor(Math.log10(rawStep.toDouble()))).toFloat()
    val normalized = rawStep / magnitude
    val niceNormalized = when {
        normalized <= 1f -> 1f
        normalized <= 2f -> 2f
        normalized <= 5f -> 5f
        else -> 10f
    }
    return niceNormalized * magnitude
}

private fun computeAxisTicks(items: List<MonthlyTotal>, tickCount: Int = 4): List<Int> {
    val minAmount = items.minOf { it.amount }
    val maxAmount = items.maxOf { it.amount }
    val step = if (minAmount == maxAmount) {
        niceAxisStep(maxAmount.coerceAtLeast(1) / tickCount.toFloat())
    } else {
        niceAxisStep((maxAmount - minAmount) / tickCount.toFloat())
    }
    val axisMin = (kotlin.math.floor(minAmount / step) * step).toInt().coerceAtLeast(0)
    return (0..tickCount).map { axisMin + (step * it).toInt() }
}

@Composable
fun MonthlyTrendChart(items: List<MonthlyTotal>, modifier: Modifier = Modifier) {
    if (items.size < 2) return
    val ticks = remember(items) { computeAxisTicks(items) }
    val axisMin = ticks.first()
    val axisMax = ticks.last()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            Column(
                modifier = Modifier.width(52.dp).fillMaxHeight(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                ticks.reversed().forEach { tick ->
                    Text(
                        formatWon(tick),
                        style = MaterialTheme.typography.labelSmall,
                        color = HPText,
                        maxLines = 1
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val stepX = size.width / (items.size - 1)
                val topInset = size.height * 0.06f
                val usableHeight = size.height * 0.88f
                val axisRange = (axisMax - axisMin).coerceAtLeast(1)
                fun yFor(amount: Int): Float {
                    val t = (amount - axisMin) / axisRange.toFloat()
                    return topInset + usableHeight - (t * usableHeight)
                }
                ticks.forEach { tick ->
                    val y = yFor(tick)
                    drawLine(HPGray4, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                }
                val points = items.mapIndexed { index, item -> Offset(index * stepX, yFor(item.amount)) }
                val linePath = Path().apply {
                    points.forEachIndexed { index, point ->
                        if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                    }
                }
                val fillPath = Path().apply {
                    addPath(linePath)
                    lineTo(points.last().x, size.height)
                    lineTo(points.first().x, size.height)
                    close()
                }
                drawPath(fillPath, brush = Brush.verticalGradient(listOf(HPMain.copy(alpha = 0.2f), Color.Transparent)))
                drawPath(linePath, color = HPMain, style = Stroke(width = 3.dp.toPx()))
                points.forEach { point -> drawCircle(color = HPMain, radius = 4.dp.toPx(), center = point) }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(start = 60.dp)) {
            items.forEach { item ->
                Text(
                    stringResource(R.string.expenseanalysis_monthly_month_axis_format, item.month.monthValue),
                    style = MaterialTheme.typography.labelMedium,
                    color = HPText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "월별 지출 추이 그래프")
@Composable
private fun MonthlyTrendChartPreview() {
    HampouchTheme {
        MonthlyTrendChart(
            items = listOf(
                MonthlyTotal(month = YearMonth.of(2026, 3), amount = 180_000),
                MonthlyTotal(month = YearMonth.of(2026, 4), amount = 210_000),
                MonthlyTotal(month = YearMonth.of(2026, 5), amount = 150_000)
            )
        )
    }
}

@Composable
fun AnalysisTabRow(
    tabs: List<Pair<String, String>>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { (id, label) ->
            val selected = id == selectedId
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) HPMain else HPWhite)
                    .border(width = 1.dp, color = if (selected) HPMain else HPGray4, shape = RoundedCornerShape(50))
                    .clickable { onSelect(id) }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) HPWhite else HPBlack,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "분석 탭 행")
@Composable
private fun AnalysisTabRowPreview() {
    HampouchTheme {
        AnalysisTabRow(
            tabs = listOf("all" to "전체", "delivery" to "배달음식", "cafe" to "카페"),
            selectedId = "all",
            onSelect = {}
        )
    }
}

@Composable
fun DetailSpentSummaryCard(
    titleFormatResId: Int,
    label: String,
    amount: Int,
    count: Int,
    percent: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub4)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(titleFormatResId, withRoParticle(label)), style = MaterialTheme.typography.bodyMedium, color = HPText)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                stringResource(R.string.expensedetail_amount_won_format, formatWon(amount)),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = HPMain
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                stringResource(R.string.expenseanalysis_count_format, count),
                style = MaterialTheme.typography.bodySmall,
                color = HPText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(HPMain)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    stringResource(R.string.expenseanalysis_percent_format, percent),
                    style = MaterialTheme.typography.labelMedium,
                    color = HPWhite
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "상세 지출 요약 카드")
@Composable
private fun DetailSpentSummaryCardPreview() {
    HampouchTheme {
        DetailSpentSummaryCard(
            titleFormatResId = R.string.expenseanalysis_category_detail_spent_format,
            label = "배달음식",
            amount = 40_000,
            count = 6,
            percent = 40
        )
    }
}

@Composable
fun ExpenseAnalysisListItem(
    record: ExpenseRecord,
    showReasonChip: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryLabel = resolveAnalysisCategoryLabel(record)
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
        Text(
            stringResource(R.string.expenseanalysis_date_short_format, record.date.monthValue, record.date.dayOfMonth),
            style = MaterialTheme.typography.labelMedium,
            color = HPText,
            modifier = Modifier.width(40.dp)
        )
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
        Column(horizontalAlignment = Alignment.End) {
            if (showReasonChip && reasonLabel != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(HPSub3)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(reasonLabel, style = MaterialTheme.typography.labelMedium, color = HPText)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                stringResource(R.string.expensedetail_amount_won_format, formatWon(record.amount)),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = HPBlack
            )
        }
    }
}

@Preview(showBackground = true, name = "지출 분석 목록 아이템")
@Composable
private fun ExpenseAnalysisListItemPreview() {
    HampouchTheme {
        ExpenseAnalysisListItem(
            record = ExpenseRecord(
                id = "1",
                date = LocalDate.of(2026, 5, 3),
                amount = 4_500,
                categoryId = "cafe",
                expenseName = "스타벅스",
                reasonId = "stress"
            ),
            showReasonChip = true,
            onClick = {}
        )
    }
}

@Composable
fun weekdayFullNames(days: List<DayOfWeek>): List<String> =
    days.map { it.getDisplayName(TextStyle.FULL, Locale.KOREA) }

@Composable
fun PochiAnalysisCard(
    periodLabel: String,
    totalAmount: Int,
    topCategoryLabels: String,
    topCategoryPercent: Int,
    topReasonLabel: String,
    topReasonPercent: Int,
    peakWeekdayLabels: String?,
    modifier: Modifier = Modifier,
    serverInsight: String? = null
) {
    val highlight = SpanStyle(color = HPMain, fontWeight = FontWeight.Bold)
    val message = if (serverInsight != null) {
        buildAnnotatedString { append(serverInsight) }
    } else {
        buildAnnotatedString {
            append(stringResource(R.string.expenseanalysis_ai_intro_prefix_format, periodLabel))
            withStyle(highlight) { append(stringResource(R.string.expensedetail_amount_won_format, formatWon(totalAmount))) }
            append(stringResource(R.string.expenseanalysis_ai_intro_suffix))
            withStyle(highlight) { append(topCategoryLabels) }
            append(" ")
            append(stringResource(R.string.expenseanalysis_ai_category_middle))
            withStyle(highlight) { append(stringResource(R.string.expenseanalysis_percent_format, topCategoryPercent)) }
            append(stringResource(R.string.expenseanalysis_ai_category_suffix))
            withStyle(highlight) { append(topReasonLabel) }
            append(stringResource(R.string.expenseanalysis_ai_reason_middle))
            withStyle(highlight) { append(stringResource(R.string.expenseanalysis_percent_format, topReasonPercent)) }
            append(stringResource(R.string.expenseanalysis_ai_reason_suffix))
            if (!peakWeekdayLabels.isNullOrEmpty()) {
                append(peakWeekdayLabels)
                append(stringResource(R.string.expenseanalysis_ai_weekday_suffix))
            }
            append(stringResource(R.string.expenseanalysis_ai_closing))
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub4)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.img_hamster_analysis),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(76.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(stringResource(R.string.expenseanalysis_ai_section_title), style = Body16Bold, color = HPMain)
        }
        DashedDivider(color = HPSub2)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = HPBlack
        )
    }
}

@Preview(showBackground = true, name = "포치 지출 분석 카드")
@Composable
private fun PochiAnalysisCardPreview() {
    HampouchTheme {
        PochiAnalysisCard(
            periodLabel = "이번 달",
            totalAmount = 150_000,
            topCategoryLabels = "배달음식",
            topCategoryPercent = 40,
            topReasonLabel = "스트레스",
            topReasonPercent = 35,
            peakWeekdayLabels = "화요일, 금요일"
        )
    }
}

@Composable
fun monthYearLabel(month: YearMonth): String =
    stringResource(R.string.expensedetail_calendar_year_month_format, month.year, month.monthValue)

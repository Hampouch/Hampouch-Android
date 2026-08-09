package com.example.hampouch.ui.expenseanalysis

import com.example.hampouch.data.model.ExpenseRecord
import com.example.hampouch.ui.expensedetail.ExpenseReasonCatalog
import com.example.hampouch.ui.home.HomeCategoryCatalog
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

const val ExpenseAnalysisEtcId = "etc"

data class AmountBreakdownItem(
    val id: String,
    val amount: Int,
    val percent: Int
)

data class WeekdayAmount(
    val dayOfWeek: DayOfWeek,
    val amount: Int
)

data class MonthlyTotal(
    val month: YearMonth,
    val amount: Int
)

data class DailyAmount(
    val date: LocalDate,
    val amount: Int
)

data class ExpensePeriodSummary(
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val totalAmount: Int,
    val dailyAverage: Int,
    val dailyBreakdown: List<DailyAmount>
)

data class ExpenseAnalysisSummary(
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val totalAmount: Int,
    val categoryBreakdown: List<AmountBreakdownItem>,
    val reasonBreakdown: List<AmountBreakdownItem>,
    val weekdayBreakdown: List<WeekdayAmount>,
    val weekdayInsight: String?,
    val pouchInsight: String?
)

data class ExpenseTagAnalysisResult(
    val id: String,
    val totalAmount: Int,
    val count: Int,
    val percent: Int,
    val records: List<ExpenseRecord>
)

data class ExpenseTrendResult(
    val month: YearMonth,
    val totalAmount: Int,
    val monthlyAverage: Int,
    val diffRateFromLastMonth: Int?,
    val trend: List<MonthlyTotal>,
    val trendInsight: String?
)

val ExpenseAnalysisCategoryLegendOrder: List<String> =
    listOf("delivery", "snack", "dining_out", "mart", "convenience", "drink", "cafe", ExpenseAnalysisEtcId)

val ExpenseAnalysisCategoryTabOrder: List<String> =
    HomeCategoryCatalog.categories.map { it.id }

val ExpenseAnalysisReasonTabOrder: List<String> =
    ExpenseReasonCatalog.reasons.map { it.id } + ExpenseAnalysisEtcId

val ExpenseAnalysisWeekdayOrder: List<DayOfWeek> =
    listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    )

private fun categoryBucketOf(record: ExpenseRecord): String =
    record.categoryId?.takeIf { HomeCategoryCatalog.byId(it) != null } ?: ExpenseAnalysisEtcId

private fun reasonBucketOf(record: ExpenseRecord): String =
    record.reasonId?.takeIf { ExpenseReasonCatalog.byId(it) != null } ?: ExpenseAnalysisEtcId

fun List<ExpenseRecord>.inPeriod(start: LocalDate, end: LocalDate): List<ExpenseRecord> =
    filter { !it.date.isBefore(start) && !it.date.isAfter(end) }

fun List<ExpenseRecord>.categoryBreakdown(order: List<String> = ExpenseAnalysisCategoryLegendOrder): List<AmountBreakdownItem> {
    val total = sumOf { it.amount }
    val amountByBucket = groupBy { categoryBucketOf(it) }.mapValues { (_, records) -> records.sumOf { it.amount } }
    return order.map { id ->
        val amount = amountByBucket[id] ?: 0
        AmountBreakdownItem(id, amount, percentOf(amount, total))
    }
}

fun List<ExpenseRecord>.reasonBreakdown(order: List<String> = ExpenseAnalysisReasonTabOrder): List<AmountBreakdownItem> {
    val total = sumOf { it.amount }
    val amountByBucket = groupBy { reasonBucketOf(it) }.mapValues { (_, records) -> records.sumOf { it.amount } }
    return order.map { id ->
        val amount = amountByBucket[id] ?: 0
        AmountBreakdownItem(id, amount, percentOf(amount, total))
    }
}

fun List<ExpenseRecord>.recordsForCategory(categoryId: String): List<ExpenseRecord> =
    filter { categoryBucketOf(it) == categoryId }.sortedByDescending { it.date }

fun List<ExpenseRecord>.recordsForReason(reasonId: String): List<ExpenseRecord> =
    filter { reasonBucketOf(it) == reasonId }.sortedByDescending { it.date }

fun List<ExpenseRecord>.weekdayBreakdown(): List<WeekdayAmount> {
    val amountByDay = groupBy { it.date.dayOfWeek }.mapValues { (_, records) -> records.sumOf { it.amount } }
    return ExpenseAnalysisWeekdayOrder.map { WeekdayAmount(it, amountByDay[it] ?: 0) }
}

fun List<ExpenseRecord>.peakWeekdays(maxCount: Int = 2): List<DayOfWeek> {
    val breakdown = weekdayBreakdown()
    val maxAmount = breakdown.maxOfOrNull { it.amount } ?: 0
    if (maxAmount <= 0) return emptyList()
    return breakdown.filter { it.amount == maxAmount }.map { it.dayOfWeek }.take(maxCount)
}

fun List<ExpenseRecord>.monthlyTotals(referenceToday: LocalDate, monthCount: Int = 6): List<MonthlyTotal> {
    val startMonth = YearMonth.from(referenceToday).minusMonths((monthCount - 1).toLong())
    val amountByMonth = groupBy { YearMonth.from(it.date) }.mapValues { (_, records) -> records.sumOf { it.amount } }
    return (0 until monthCount).map { offset ->
        val month = startMonth.plusMonths(offset.toLong())
        MonthlyTotal(month, amountByMonth[month] ?: 0)
    }
}

private fun percentOf(amount: Int, total: Int): Int =
    if (total <= 0) 0 else Math.round(amount * 100f / total)

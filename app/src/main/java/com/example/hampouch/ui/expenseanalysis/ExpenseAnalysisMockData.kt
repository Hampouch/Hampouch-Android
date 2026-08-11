package com.example.hampouch.ui.expenseanalysis

import com.example.hampouch.domain.model.AmountBreakdownItem
import com.example.hampouch.domain.model.DailyAmount
import com.example.hampouch.domain.model.ExpenseAnalysisEtcId
import com.example.hampouch.domain.model.ExpenseAnalysisSummary
import com.example.hampouch.domain.model.ExpenseCategoryIds
import com.example.hampouch.domain.model.ExpensePeriodSummary
import com.example.hampouch.domain.model.ExpenseReasonIds
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.ExpenseTagAnalysisResult
import com.example.hampouch.domain.model.ExpenseTrendResult
import com.example.hampouch.domain.model.ExpenseWeekdayOrder
import com.example.hampouch.domain.model.MonthlyTotal
import com.example.hampouch.domain.model.WeekdayAmount
import com.example.hampouch.ui.expensedetail.ExpenseReasonCatalog
import com.example.hampouch.ui.home.HomeCategoryCatalog
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

// 분석 결과 모델과 카테고리·이유 id 목록은 domain/model/ExpenseAnalysis.kt로 옮겼다.
// 화면에서 쓰던 이름은 아래 별칭으로 유지한다.

val ExpenseAnalysisCategoryLegendOrder: List<String> = ExpenseCategoryIds

val ExpenseAnalysisCategoryTabOrder: List<String> =
    HomeCategoryCatalog.categories.map { it.id }

val ExpenseAnalysisReasonTabOrder: List<String> = ExpenseReasonIds

val ExpenseAnalysisWeekdayOrder: List<DayOfWeek> = ExpenseWeekdayOrder

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

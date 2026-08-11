package com.example.hampouch.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * 지출 내역을 분석 결과로 집계하는 순수 계산들.
 *
 * 목데이터 모드에서 서버 응답 대신 쓰이고, 화면에서도 직접 쓴다.
 * 예전에는 화면 패키지에 있었지만 카테고리·이유 분류 기준은 도메인 규칙이라 여기로 옮겼다.
 */

private fun categoryBucketOf(record: ExpenseRecord): String =
    record.categoryId?.takeIf { it in ExpenseCategoryIds } ?: ExpenseAnalysisEtcId

private fun reasonBucketOf(record: ExpenseRecord): String =
    record.reasonId?.takeIf { it in ExpenseReasonIds } ?: ExpenseAnalysisEtcId

private fun percentOf(amount: Int, total: Int): Int =
    if (total <= 0) 0 else Math.round(amount * 100f / total)

fun List<ExpenseRecord>.inPeriod(start: LocalDate, end: LocalDate): List<ExpenseRecord> =
    filter { !it.date.isBefore(start) && !it.date.isAfter(end) }

fun List<ExpenseRecord>.categoryBreakdown(
    order: List<String> = ExpenseCategoryIds
): List<AmountBreakdownItem> {
    val total = sumOf { it.amount }
    val amountByBucket = groupBy { categoryBucketOf(it) }
        .mapValues { (_, records) -> records.sumOf { it.amount } }
    return order.map { id ->
        val amount = amountByBucket[id] ?: 0
        AmountBreakdownItem(id, amount, percentOf(amount, total))
    }
}

fun List<ExpenseRecord>.reasonBreakdown(
    order: List<String> = ExpenseReasonIds
): List<AmountBreakdownItem> {
    val total = sumOf { it.amount }
    val amountByBucket = groupBy { reasonBucketOf(it) }
        .mapValues { (_, records) -> records.sumOf { it.amount } }
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
    val amountByDay = groupBy { it.date.dayOfWeek }
        .mapValues { (_, records) -> records.sumOf { it.amount } }
    return ExpenseWeekdayOrder.map { WeekdayAmount(it, amountByDay[it] ?: 0) }
}

fun List<ExpenseRecord>.peakWeekdays(maxCount: Int = 2): List<DayOfWeek> {
    val breakdown = weekdayBreakdown()
    val maxAmount = breakdown.maxOfOrNull { it.amount } ?: 0
    if (maxAmount <= 0) return emptyList()
    return breakdown.filter { it.amount == maxAmount }.map { it.dayOfWeek }.take(maxCount)
}

fun List<ExpenseRecord>.monthlyTotals(
    referenceToday: LocalDate,
    monthCount: Int = 6
): List<MonthlyTotal> {
    val startMonth = YearMonth.from(referenceToday).minusMonths((monthCount - 1).toLong())
    val amountByMonth = groupBy { YearMonth.from(it.date) }
        .mapValues { (_, records) -> records.sumOf { it.amount } }
    return (0 until monthCount).map { offset ->
        val month = startMonth.plusMonths(offset.toLong())
        MonthlyTotal(month, amountByMonth[month] ?: 0)
    }
}

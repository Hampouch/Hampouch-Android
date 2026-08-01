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

val ExpenseAnalysisCategoryLegendOrder: List<String> =
    listOf("delivery", "snack", "dining_out", "mart", "convenience", "drink", "cafe", ExpenseAnalysisEtcId)

val ExpenseAnalysisCategoryTabOrder: List<String> =
    HomeCategoryCatalog.categories.map { it.id } + ExpenseAnalysisEtcId

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

object ExpenseAnalysisMockData {

    private val storeNamesByCategory: Map<String, List<String>> = mapOf(
        "delivery" to listOf("배달의민족", "요기요", "쿠팡이츠"),
        "dining_out" to listOf("연남동 국밥집", "한신 포차", "역전 우동"),
        "convenience" to listOf("GS25", "CU", "세븐일레븐"),
        "cafe" to listOf("스타벅스", "메가커피", "이디야"),
        "snack" to listOf("올리브영 간식", "다이소 과자", "무인 간식점"),
        "mart" to listOf("이마트24", "홈플러스 익스프레스", "동네 마트"),
        "drink" to listOf("호프집", "포장마차", "이자카야")
    )

    private val reasonCycle = ExpenseReasonCatalog.reasons.map { it.id }
    private val categoryCycle = HomeCategoryCatalog.categories.map { it.id }

    fun records(referenceToday: LocalDate = LocalDate.now()): List<ExpenseRecord> {
        val startMonth = YearMonth.from(referenceToday).minusMonths(5)
        val result = mutableListOf<ExpenseRecord>()
        var counter = 0

        var month = startMonth
        while (!month.isAfter(YearMonth.from(referenceToday))) {
            var day = 1
            while (day <= month.lengthOfMonth()) {
                val date = month.atDay(day)
                if (!date.isAfter(referenceToday)) {
                    counter++
                    result += buildRecord(counter, date)
                }
                day += 2
            }
            month = month.plusMonths(1)
        }
        return result
    }

    private fun buildRecord(counter: Int, date: LocalDate): ExpenseRecord {
        val isWeekendPeak = date.dayOfWeek == DayOfWeek.FRIDAY || date.dayOfWeek == DayOfWeek.SATURDAY
        val baseAmount = 6_000 + (counter % 6) * 3_000
        val amount = if (isWeekendPeak) (baseAmount * 1.8).toInt() else baseAmount

        val useCustomCategory = counter % 9 == 0
        val categoryId = categoryCycle[counter % categoryCycle.size]
        val storeName = storeNamesByCategory[categoryId]?.get(counter % 3)

        val reasonSlot = counter % 6
        val reasonId = if (reasonSlot < reasonCycle.size) reasonCycle[reasonSlot] else null
        val useCustomReason = reasonId == null && reasonSlot % 2 == 0

        return ExpenseRecord(
            id = "analysis_$counter",
            date = date,
            amount = amount,
            categoryId = if (useCustomCategory) null else categoryId,
            customCategoryName = if (useCustomCategory) "자취 생활비" else null,
            expenseName = if (useCustomCategory) null else storeName,
            reasonId = reasonId,
            customReason = if (useCustomReason) "그때 기분" else null
        )
    }
}

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

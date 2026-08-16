package com.example.hampouch.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

const val ExpenseAnalysisEtcId = "etc"

val ExpenseCategoryIds: List<String> =
    listOf("delivery", "snack", "dining_out", "mart", "convenience", "drink", "cafe", ExpenseAnalysisEtcId)

val ExpenseReasonIds: List<String> =
    listOf("stress", "reward", "lazy", "craving", ExpenseAnalysisEtcId)

val ExpenseWeekdayOrder: List<DayOfWeek> = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
)

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

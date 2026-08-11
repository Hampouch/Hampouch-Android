package com.example.hampouch.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/** 카테고리·이유가 특정되지 않은 항목을 묶는 id. 서버의 "ETC"에 대응한다. */
const val ExpenseAnalysisEtcId = "etc"

/**
 * 지출 카테고리 id 목록. 서버 enum과의 매핑 기준이자 분석 화면 범례 순서다.
 * 아이콘·색·표시 문구는 표현 계층([com.example.hampouch.ui.home.HomeCategoryCatalog])이 갖는다.
 */
val ExpenseCategoryIds: List<String> =
    listOf("delivery", "snack", "dining_out", "mart", "convenience", "drink", "cafe", ExpenseAnalysisEtcId)

/** 지출 이유 id 목록. 표시 문구는 [com.example.hampouch.ui.expensedetail.ExpenseReasonCatalog]가 갖는다. */
val ExpenseReasonIds: List<String> =
    listOf("stress", "reward", "lazy", "craving", ExpenseAnalysisEtcId)

/** 분석 화면의 요일 정렬 순서(월~일). */
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

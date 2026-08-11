package com.example.hampouch.domain.repository

import com.example.hampouch.domain.model.ExpenseAnalysisSummary
import com.example.hampouch.domain.model.ExpensePeriodSummary
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.ExpenseTagAnalysisResult
import com.example.hampouch.domain.model.ExpenseTrendResult
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.YearMonth

/**
 * 지출 도메인.
 *
 * [records]는 앱이 지금까지 받아 둔 지출 내역 캐시다. 화면은 이 흐름을 구독하고,
 * 필요한 시점에 load* 를 불러 서버와 맞춘다.
 */
interface ExpenseRepository {

    /** id → 지출 내역 캐시. */
    val records: StateFlow<Map<String, ExpenseRecord>>

    fun recordsForDate(date: LocalDate): List<ExpenseRecord>

    fun byId(id: String): ExpenseRecord?

    suspend fun createExpense(record: ExpenseRecord): Result<ExpenseRecord>

    suspend fun updateExpense(record: ExpenseRecord): Result<ExpenseRecord>

    suspend fun deleteExpense(id: String): Result<Unit>

    suspend fun loadExpenseDetail(id: String): Result<ExpenseRecord>

    /** [date] 하루치 목록을 서버에서 받아 캐시의 해당 날짜를 통째로 교체한다. */
    suspend fun loadDay(date: LocalDate): Result<Unit>

    suspend fun loadWeekSummary(standardDate: LocalDate): Result<ExpensePeriodSummary>

    suspend fun loadMonthSummary(standardMonth: YearMonth): Result<ExpensePeriodSummary>

    suspend fun loadAnalysis(periodStart: LocalDate, periodEnd: LocalDate): Result<ExpenseAnalysisSummary>

    suspend fun loadCategoryAnalysis(
        categoryId: String,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): Result<ExpenseTagAnalysisResult>

    suspend fun loadEmotionAnalysis(
        reasonId: String,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): Result<ExpenseTagAnalysisResult>

    suspend fun loadTrend(month: YearMonth): Result<ExpenseTrendResult>

    /**
     * "오늘 지출 없음"을 기록한다. 대응하는 서버 API가 없어 로컬 캐시에만 0원 항목을 남긴다
     * — 앱을 다시 켜면 사라진다.
     */
    fun markNoSpending(date: LocalDate)

    fun resetForAccount()
}

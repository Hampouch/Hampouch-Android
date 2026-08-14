package com.example.hampouch.domain.repository

import com.example.hampouch.domain.model.ExpenseAnalysisSummary
import com.example.hampouch.domain.model.ExpensePeriodSummary
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.ExpenseTagAnalysisResult
import com.example.hampouch.domain.model.ExpenseTrendResult
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.YearMonth

interface ExpenseRepository {

    val records: StateFlow<Map<String, ExpenseRecord>>

    fun recordsForDate(date: LocalDate): List<ExpenseRecord>

    fun byId(id: String): ExpenseRecord?

    suspend fun createExpense(record: ExpenseRecord): Result<ExpenseRecord>

    suspend fun updateExpense(record: ExpenseRecord): Result<ExpenseRecord>

    suspend fun deleteExpense(id: String): Result<Unit>

    suspend fun loadExpenseDetail(id: String): Result<ExpenseRecord>

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
     * 그 날짜의 입력을 마쳤는지(지출이 있거나 "오늘은 안 썼어요"를 눌렀는지).
     * 서버 `GET /api/expenses/day`의 `hasRecord`로 채워진다.
     */
    val daysWithRecord: StateFlow<Set<LocalDate>>

    /**
     * "오늘은 안 썼어요"를 기록한다(PUT /api/expenses/no-spend).
     * 0원 지출을 만드는 게 아니라 해당 날짜를 지출 없이 마감했다는 별도 기록이라,
     * 지출 목록([records])에는 포함되지 않는다.
     */
    suspend fun markNoSpend(date: LocalDate): Result<Unit>

    fun resetForAccount()
}

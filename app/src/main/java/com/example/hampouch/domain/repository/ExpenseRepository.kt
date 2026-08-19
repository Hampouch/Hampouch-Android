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

    val daysWithRecord: StateFlow<Set<LocalDate>>

    suspend fun markNoSpend(date: LocalDate): Result<Unit>

    fun resetForAccount()
}

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

val ExpenseAnalysisCategoryLegendOrder: List<String> = ExpenseCategoryIds

val ExpenseAnalysisCategoryTabOrder: List<String> =
    HomeCategoryCatalog.categories.map { it.id }

val ExpenseAnalysisReasonTabOrder: List<String> = ExpenseReasonIds

val ExpenseAnalysisWeekdayOrder: List<DayOfWeek> = ExpenseWeekdayOrder


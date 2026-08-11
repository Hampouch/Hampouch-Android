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


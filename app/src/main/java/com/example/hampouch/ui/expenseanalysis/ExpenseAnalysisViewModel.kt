package com.example.hampouch.ui.expenseanalysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.ExpenseAnalysisSummary
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.ExpenseTagAnalysisResult
import com.example.hampouch.domain.model.ExpenseTrendResult
import com.example.hampouch.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * 지출 분석 화면들(기간 분석·월별 추이·카테고리 상세·이유 상세)이 공유하는 ViewModel.
 *
 * 목데이터 모드에서는 서버 결과가 비고, 화면이 [records]로부터 직접 계산한다.
 */
@HiltViewModel
class ExpenseAnalysisViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    val records: StateFlow<List<ExpenseRecord>> = expenseRepository.records
        .map { it.values.toList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _summary = MutableStateFlow<ExpenseAnalysisSummary?>(null)
    val summary: StateFlow<ExpenseAnalysisSummary?> = _summary.asStateFlow()

    private val _trend = MutableStateFlow<ExpenseTrendResult?>(null)
    val trend: StateFlow<ExpenseTrendResult?> = _trend.asStateFlow()

    private val _tagResult = MutableStateFlow<ExpenseTagAnalysisResult?>(null)
    val tagResult: StateFlow<ExpenseTagAnalysisResult?> = _tagResult.asStateFlow()

    fun loadSummary(periodStart: LocalDate, periodEnd: LocalDate) {
        viewModelScope.launch {
            _summary.value = expenseRepository.loadAnalysis(periodStart, periodEnd).getOrNull()
        }
    }

    fun loadTrend(month: YearMonth) {
        viewModelScope.launch {
            _trend.value = expenseRepository.loadTrend(month).getOrNull()
        }
    }

    fun loadCategoryAnalysis(categoryId: String, periodStart: LocalDate, periodEnd: LocalDate) {
        viewModelScope.launch {
            _tagResult.value =
                expenseRepository.loadCategoryAnalysis(categoryId, periodStart, periodEnd).getOrNull()
        }
    }

    fun loadEmotionAnalysis(reasonId: String, periodStart: LocalDate, periodEnd: LocalDate) {
        viewModelScope.launch {
            _tagResult.value =
                expenseRepository.loadEmotionAnalysis(reasonId, periodStart, periodEnd).getOrNull()
        }
    }
}

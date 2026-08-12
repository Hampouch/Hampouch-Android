package com.example.hampouch.ui.expenseanalysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.ExpenseAnalysisSummary
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.ExpenseTagAnalysisResult
import com.example.hampouch.domain.model.ExpenseTrendResult
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.ui.common.LoadState
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

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()
    private var retryAction: (() -> Unit)? = null

    fun retry() = retryAction?.invoke()

    fun loadSummary(periodStart: LocalDate, periodEnd: LocalDate) {
        retryAction = { loadSummary(periodStart, periodEnd) }
        _loadState.value = LoadState.Loading
        viewModelScope.launch {
            expenseRepository.loadAnalysis(periodStart, periodEnd)
                .onSuccess {
                    _summary.value = it
                    _loadState.value = LoadState.Content(it.totalAmount == 0)
                }
                .onFailure { _loadState.value = LoadState.Failure(it.toUserMessage("지출 분석을 불러오지 못했습니다.")) }
        }
    }

    fun loadTrend(month: YearMonth) {
        retryAction = { loadTrend(month) }
        _loadState.value = LoadState.Loading
        viewModelScope.launch {
            expenseRepository.loadTrend(month)
                .onSuccess {
                    _trend.value = it
                    _loadState.value = LoadState.Content(it.trend.isEmpty())
                }
                .onFailure { _loadState.value = LoadState.Failure(it.toUserMessage("월별 추이를 불러오지 못했습니다.")) }
        }
    }

    fun loadCategoryAnalysis(categoryId: String, periodStart: LocalDate, periodEnd: LocalDate) {
        retryAction = { loadCategoryAnalysis(categoryId, periodStart, periodEnd) }
        _loadState.value = LoadState.Loading
        viewModelScope.launch {
            expenseRepository.loadCategoryAnalysis(categoryId, periodStart, periodEnd)
                .onSuccess {
                    _tagResult.value = it
                    _loadState.value = LoadState.Content(it.records.isEmpty())
                }
                .onFailure { _loadState.value = LoadState.Failure(it.toUserMessage("카테고리 분석을 불러오지 못했습니다.")) }
        }
    }

    fun loadEmotionAnalysis(reasonId: String, periodStart: LocalDate, periodEnd: LocalDate) {
        retryAction = { loadEmotionAnalysis(reasonId, periodStart, periodEnd) }
        _loadState.value = LoadState.Loading
        viewModelScope.launch {
            expenseRepository.loadEmotionAnalysis(reasonId, periodStart, periodEnd)
                .onSuccess {
                    _tagResult.value = it
                    _loadState.value = LoadState.Content(it.records.isEmpty())
                }
                .onFailure { _loadState.value = LoadState.Failure(it.toUserMessage("감정 분석을 불러오지 못했습니다.")) }
        }
    }
}

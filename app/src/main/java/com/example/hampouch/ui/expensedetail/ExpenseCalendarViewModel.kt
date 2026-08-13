package com.example.hampouch.ui.expensedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.ExpensePeriodSummary
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.ui.common.LoadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class ExpenseCalendarViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    challengeRepository: ChallengeRepository
) : ViewModel() {

    val challengeState: StateFlow<ChallengeState> = challengeRepository.state

    /** id → 지출 내역 캐시. 목데이터 모드에서 달력이 직접 집계하는 원본이다. */
    val records: StateFlow<Map<String, ExpenseRecord>> = expenseRepository.records

    private val _monthSummary = MutableStateFlow<ExpensePeriodSummary?>(null)
    val monthSummary: StateFlow<ExpensePeriodSummary?> = _monthSummary.asStateFlow()

    private val _weekSummary = MutableStateFlow<ExpensePeriodSummary?>(null)
    val weekSummary: StateFlow<ExpensePeriodSummary?> = _weekSummary.asStateFlow()

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()
    private var retryAction: (() -> Unit)? = null

    fun retry() = retryAction?.invoke()

    fun loadMonthSummary(month: YearMonth) {
        retryAction = { loadMonthSummary(month) }
        _loadState.value = LoadState.Loading
        viewModelScope.launch {
            expenseRepository.loadMonthSummary(month)
                .onSuccess {
                    _monthSummary.value = it
                    _loadState.value = LoadState.Content(it.totalAmount == 0)
                }
                .onFailure { _loadState.value = LoadState.Failure(it.toUserMessage("월간 기록을 불러오지 못했습니다.")) }
        }
    }

    fun loadWeekSummary(weekStart: LocalDate) {
        retryAction = { loadWeekSummary(weekStart) }
        _loadState.value = LoadState.Loading
        viewModelScope.launch {
            expenseRepository.loadWeekSummary(weekStart)
                .onSuccess {
                    _weekSummary.value = it
                    _loadState.value = LoadState.Content(it.totalAmount == 0)
                }
                .onFailure { _loadState.value = LoadState.Failure(it.toUserMessage("주간 기록을 불러오지 못했습니다.")) }
        }
    }

    fun loadDay(date: LocalDate) {
        retryAction = { loadDay(date) }
        _loadState.value = LoadState.Loading
        viewModelScope.launch {
            expenseRepository.loadDay(date)
                .onSuccess { _loadState.value = LoadState.Content(expenseRepository.recordsForDate(date).isEmpty()) }
                .onFailure { _loadState.value = LoadState.Failure(it.toUserMessage("일간 기록을 불러오지 못했습니다.")) }
        }
    }
}

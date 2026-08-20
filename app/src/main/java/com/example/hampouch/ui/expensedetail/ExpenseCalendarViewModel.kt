package com.example.hampouch.ui.expensedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.ExpensePeriodSummary
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.RestState
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.domain.repository.RestRepository
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
    challengeRepository: ChallengeRepository,
    restRepository: RestRepository
) : ViewModel() {

    val challengeState: StateFlow<ChallengeState> = challengeRepository.state
    val restState: StateFlow<RestState> = restRepository.restState

    val records: StateFlow<Map<String, ExpenseRecord>> = expenseRepository.records

    private val _monthSummary = MutableStateFlow<ExpensePeriodSummary?>(null)
    val monthSummary: StateFlow<ExpensePeriodSummary?> = _monthSummary.asStateFlow()

    private val _weekSummary = MutableStateFlow<ExpensePeriodSummary?>(null)
    val weekSummary: StateFlow<ExpensePeriodSummary?> = _weekSummary.asStateFlow()

    private val _monthLoadState = MutableStateFlow<LoadState>(LoadState.Idle)
    val monthLoadState: StateFlow<LoadState> = _monthLoadState.asStateFlow()
    private var monthRetryAction: (() -> Unit)? = null
    fun retryMonth() = monthRetryAction?.invoke()

    private val _weekLoadState = MutableStateFlow<LoadState>(LoadState.Idle)
    val weekLoadState: StateFlow<LoadState> = _weekLoadState.asStateFlow()
    private var weekRetryAction: (() -> Unit)? = null
    fun retryWeek() = weekRetryAction?.invoke()

    private val _dayLoadState = MutableStateFlow<LoadState>(LoadState.Idle)
    val dayLoadState: StateFlow<LoadState> = _dayLoadState.asStateFlow()
    private var dayRetryAction: (() -> Unit)? = null
    fun retryDay() = dayRetryAction?.invoke()

    fun loadMonthSummary(month: YearMonth) {
        monthRetryAction = { loadMonthSummary(month) }
        _monthLoadState.value = LoadState.Loading
        viewModelScope.launch {
            expenseRepository.loadMonthSummary(month)
                .onSuccess {
                    _monthSummary.value = it
                    _monthLoadState.value = LoadState.Content(it.totalAmount == 0L)
                }
                .onFailure { _monthLoadState.value = LoadState.Failure(it.toUserMessage("월간 기록을 불러오지 못했습니다.")) }
        }
    }

    fun loadWeekSummary(weekStart: LocalDate) {
        weekRetryAction = { loadWeekSummary(weekStart) }
        _weekLoadState.value = LoadState.Loading
        viewModelScope.launch {
            expenseRepository.loadWeekSummary(weekStart)
                .onSuccess {
                    _weekSummary.value = it
                    _weekLoadState.value = LoadState.Content(it.totalAmount == 0L)
                }
                .onFailure { _weekLoadState.value = LoadState.Failure(it.toUserMessage("주간 기록을 불러오지 못했습니다.")) }
        }
    }

    fun loadDay(date: LocalDate) {
        dayRetryAction = { loadDay(date) }
        _dayLoadState.value = LoadState.Loading
        viewModelScope.launch {
            expenseRepository.loadDay(date)
                .onSuccess { _dayLoadState.value = LoadState.Content(expenseRepository.recordsForDate(date).isEmpty()) }
                .onFailure { _dayLoadState.value = LoadState.Failure(it.toUserMessage("일간 기록을 불러오지 못했습니다.")) }
        }
    }
}

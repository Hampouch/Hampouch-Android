package com.example.hampouch.ui.expensedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.ExpensePeriodSummary
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
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

    fun loadMonthSummary(month: YearMonth) {
        viewModelScope.launch {
            _monthSummary.value = expenseRepository.loadMonthSummary(month).getOrNull()
        }
    }

    fun loadWeekSummary(weekStart: LocalDate) {
        viewModelScope.launch {
            _weekSummary.value = expenseRepository.loadWeekSummary(weekStart).getOrNull()
        }
    }

    fun loadDay(date: LocalDate) {
        viewModelScope.launch { expenseRepository.loadDay(date) }
    }
}

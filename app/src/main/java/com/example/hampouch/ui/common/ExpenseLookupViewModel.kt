package com.example.hampouch.ui.common

import androidx.lifecycle.ViewModel
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ExpenseLookupViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    challengeRepository: ChallengeRepository
) : ViewModel() {

    val challengeState: StateFlow<ChallengeState> = challengeRepository.state

    val records: StateFlow<Map<String, ExpenseRecord>> = expenseRepository.records

    fun recordsForDate(date: LocalDate): List<ExpenseRecord> = expenseRepository.recordsForDate(date)

    fun spentOnDate(date: LocalDate): Int = recordsForDate(date).sumOf { it.amount }
}

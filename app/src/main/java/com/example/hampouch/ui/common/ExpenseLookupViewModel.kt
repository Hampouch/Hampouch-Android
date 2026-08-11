package com.example.hampouch.ui.common

import androidx.lifecycle.ViewModel
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import javax.inject.Inject

/**
 * 지출 기록을 "읽기만" 하는 화면들이 쓰는 얇은 ViewModel.
 *
 * 챌린지 결과·목표 금액 조정·마이페이지처럼 자기 도메인은 따로 있으면서 집계에 지출 합계가 필요한
 * 화면들이 저장소를 직접 잡지 않도록 한다. 각 화면이 자기 ViewModel을 갖게 되면 그쪽으로 흡수한다.
 */
@HiltViewModel
class ExpenseLookupViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    val records: StateFlow<Map<String, ExpenseRecord>> = expenseRepository.records

    fun recordsForDate(date: LocalDate): List<ExpenseRecord> = expenseRepository.recordsForDate(date)

    fun spentOnDate(date: LocalDate): Int = recordsForDate(date).sumOf { it.amount }
}

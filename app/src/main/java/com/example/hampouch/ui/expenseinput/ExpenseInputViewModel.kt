package com.example.hampouch.ui.expenseinput

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ExpenseInputUiState(
    val initialDate: LocalDate = LocalDate.now(),
    val dailyLimit: Int = 0,
    val todayBalance: Int = 0,
    val isSubmitting: Boolean = false
)

sealed interface ExpenseInputEvent {
    data object Saved : ExpenseInputEvent
    data class ShowMessage(val message: String) : ExpenseInputEvent
}

@HiltViewModel
class ExpenseInputViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val challengeRepository: ChallengeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialDate: LocalDate = LocalDate.ofEpochDay(
        savedStateHandle.get<Long>("initialDateEpochDay") ?: LocalDate.now().toEpochDay()
    )

    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<ExpenseInputUiState> = _uiState.asStateFlow()

    private val _events = Channel<ExpenseInputEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private fun buildInitialState(): ExpenseInputUiState {
        val dailyLimit = challengeRepository.state.value.activeChallenge?.dailyLimitOn(initialDate) ?: 0
        val alreadySpent = expenseRepository.recordsForDate(initialDate).sumOf { it.amount }
        return ExpenseInputUiState(
            initialDate = initialDate,
            dailyLimit = dailyLimit,
            todayBalance = (dailyLimit - alreadySpent).coerceAtLeast(0)
        )
    }

    fun save(record: ExpenseRecord) {
        if (_uiState.value.isSubmitting) return
        _uiState.value = _uiState.value.copy(isSubmitting = true)
        viewModelScope.launch {
            expenseRepository.createExpense(record)
                .onSuccess { _events.send(ExpenseInputEvent.Saved) }
                .onFailure {
                    _events.send(ExpenseInputEvent.ShowMessage(it.toUserMessage("지출 입력에 실패했습니다.")))
                }
            _uiState.value = _uiState.value.copy(isSubmitting = false)
        }
    }
}

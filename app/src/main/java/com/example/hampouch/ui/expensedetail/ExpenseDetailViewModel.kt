package com.example.hampouch.ui.expensedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.ui.common.LoadState
import com.example.hampouch.ui.widget.HomeWidgetRefreshRequester
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpenseDetailUiState(
    val record: ExpenseRecord? = null,
    val isProcessing: Boolean = false,
    val loadState: LoadState = LoadState.Loading
)

sealed interface ExpenseDetailEvent {
    data object Finished : ExpenseDetailEvent

    data class ShowMessage(val message: String) : ExpenseDetailEvent
}

@HiltViewModel
class ExpenseDetailViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val homeWidgetRefreshRequester: HomeWidgetRefreshRequester,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val expenseId: String = savedStateHandle.get<String>("expenseId").orEmpty()

    private val _uiState = MutableStateFlow(ExpenseDetailUiState(record = expenseRepository.byId(expenseId)))
    val uiState: StateFlow<ExpenseDetailUiState> = _uiState.asStateFlow()

    private val _events = Channel<ExpenseDetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        _uiState.value = _uiState.value.copy(loadState = LoadState.Loading)
        viewModelScope.launch {
            expenseRepository.loadExpenseDetail(expenseId)
                .onSuccess { record ->
                    _uiState.value = _uiState.value.copy(record = record, loadState = LoadState.Content(false))
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        loadState = LoadState.Failure(it.toUserMessage("지출 상세를 불러오지 못했습니다."))
                    )
                }
        }
    }

    fun delete() {
        if (_uiState.value.isProcessing) return
        _uiState.value = _uiState.value.copy(isProcessing = true)
        viewModelScope.launch {
            expenseRepository.deleteExpense(expenseId)
                .onSuccess {
                    homeWidgetRefreshRequester.refreshAfterExpenseChange()
                    _events.send(ExpenseDetailEvent.Finished)
                }
                .onFailure {
                    _events.send(ExpenseDetailEvent.ShowMessage(it.toUserMessage("지출 내역 삭제에 실패했습니다.")))
                }
            _uiState.value = _uiState.value.copy(isProcessing = false)
        }
    }

    fun save(updated: ExpenseRecord) {
        if (_uiState.value.isProcessing) return
        _uiState.value = _uiState.value.copy(isProcessing = true)
        viewModelScope.launch {
            expenseRepository.updateExpense(updated)
                .onSuccess {
                    homeWidgetRefreshRequester.refreshAfterExpenseChange()
                    _events.send(ExpenseDetailEvent.Finished)
                }
                .onFailure {
                    _events.send(ExpenseDetailEvent.ShowMessage(it.toUserMessage("지출 내역 수정에 실패했습니다.")))
                }
            _uiState.value = _uiState.value.copy(isProcessing = false)
        }
    }
}

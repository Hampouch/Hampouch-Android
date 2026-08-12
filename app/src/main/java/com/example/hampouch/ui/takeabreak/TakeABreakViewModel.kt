package com.example.hampouch.ui.takeabreak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.BreakDuration
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.RestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TakeABreakUiState(
    val selectedDuration: BreakDuration? = BreakDuration.ONE_WEEK,
    val customDaysInput: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

/** 휴식 시작/연장이 성공해 화면을 떠나야 할 때 한 번만 전달되는 신호. */
sealed interface TakeABreakEvent {
    data object BreakStarted : TakeABreakEvent
}

@HiltViewModel
class TakeABreakViewModel @Inject constructor(
    private val restRepository: RestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TakeABreakUiState())
    val uiState: StateFlow<TakeABreakUiState> = _uiState.asStateFlow()

    private val _events = Channel<TakeABreakEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun selectDuration(duration: BreakDuration) {
        _uiState.value = _uiState.value.copy(selectedDuration = duration, customDaysInput = "")
    }

    fun changeCustomDays(value: String) {
        _uiState.value = _uiState.value.copy(
            customDaysInput = value.filter { it.isDigit() }.take(3),
            selectedDuration = null
        )
    }

    /**
     * [isExtending]이 true면 이미 휴식 중(홈 팝업의 "더 쉬기")이라 시작이 아니라 연장을 호출한다.
     */
    fun submit(isExtending: Boolean) {
        if (_uiState.value.isSubmitting) return
        val state = _uiState.value
        val customDays = state.customDaysInput.toIntOrNull()
        _uiState.value = state.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            val result = if (isExtending) {
                restRepository.extendBreak(state.selectedDuration, customDays)
            } else {
                restRepository.startBreak(state.selectedDuration, customDays)
            }
            result
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = null)
                    _events.send(TakeABreakEvent.BreakStarted)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = error.toUserMessage("휴식 처리에 실패했습니다.")
                    )
                }
        }
    }
}

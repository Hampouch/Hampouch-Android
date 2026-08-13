package com.example.hampouch.ui.takeabreak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.BreakDuration
import com.example.hampouch.domain.model.RestPeriod
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

sealed interface RestPeriodSelection {
    data class Preset(val duration: BreakDuration) : RestPeriodSelection
    data class Custom(val input: String) : RestPeriodSelection
}

data class TakeABreakUiState(
    val selection: RestPeriodSelection = RestPeriodSelection.Preset(BreakDuration.ONE_WEEK),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
) {
    val selectedDuration: BreakDuration?
        get() = (selection as? RestPeriodSelection.Preset)?.duration
    val customDaysInput: String
        get() = (selection as? RestPeriodSelection.Custom)?.input.orEmpty()
}

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
        _uiState.value = _uiState.value.copy(selection = RestPeriodSelection.Preset(duration))
    }

    fun changeCustomDays(value: String) {
        _uiState.value = _uiState.value.copy(
            selection = RestPeriodSelection.Custom(value.filter { it.isDigit() }.take(3))
        )
    }

    /**
     * [isExtending]이 true면 이미 휴식 중(홈 팝업의 "더 쉬기")이라 시작이 아니라 연장을 호출한다.
     */
    fun submit(isExtending: Boolean) {
        if (_uiState.value.isSubmitting) return
        val state = _uiState.value
        val period = when (val selection = state.selection) {
            is RestPeriodSelection.Preset -> RestPeriod.Preset(selection.duration)
            is RestPeriodSelection.Custom -> selection.input.toIntOrNull()
                ?.takeIf { it > 0 }
                ?.let(RestPeriod::Custom)
                ?: run {
                    _uiState.value = state.copy(errorMessage = "휴식 기간을 입력해주세요.")
                    return
                }
        }
        _uiState.value = state.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            val result = if (isExtending) {
                restRepository.extendBreak(period)
            } else {
                restRepository.startBreak(period)
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

package com.example.hampouch.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.RestState
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.domain.repository.RestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** 홈에서 한 번만 처리해야 하는 신호. */
sealed interface HomeEvent {
    /** 휴식에서 "지금 바로 복귀"에 성공 — 새 챌린지 시작 화면으로 보낸다. */
    data object ResumedFromBreak : HomeEvent

    data class ShowMessage(val message: String) : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val restRepository: RestRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    val restState: StateFlow<RestState> = restRepository.restState

    /** id → 지출 내역 캐시. 홈은 선택한 날짜/오늘 기준으로 걸러 쓴다. */
    val records: StateFlow<Map<String, ExpenseRecord>> = expenseRepository.records

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        // 휴식 도메인엔 상태 조회 API가 없어, 앱 재시작 후에는 챌린지 조회의 rest 블록으로 보정해야 한다.
        viewModelScope.launch { restRepository.syncStatus() }
    }

    fun resumeNow() {
        viewModelScope.launch {
            restRepository.resumeNow()
                .onSuccess { _events.send(HomeEvent.ResumedFromBreak) }
                .onFailure { _events.send(HomeEvent.ShowMessage("휴식 복귀에 실패했습니다.")) }
        }
    }

    /** [date]의 지출 목록을 서버와 맞춘다. */
    fun loadDay(date: LocalDate) {
        viewModelScope.launch {
            expenseRepository.loadDay(date).onFailure { error ->
                _events.send(HomeEvent.ShowMessage(error.toUserMessage("지출 내역 조회에 실패했습니다.")))
            }
        }
    }

    fun markNoSpending(date: LocalDate) = expenseRepository.markNoSpending(date)

    fun postponeOneDay() {
        viewModelScope.launch {
            restRepository.postponeOneDay()
                .onFailure { _events.send(HomeEvent.ShowMessage("복귀 연기에 실패했습니다.")) }
        }
    }
}

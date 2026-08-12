package com.example.hampouch.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.data.repository.AuthRepository
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.RestState
import com.example.hampouch.domain.model.User
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.domain.repository.RestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface HomeEvent {
    data object ResumedFromBreak : HomeEvent

    data object ChallengeEndAcknowledged : HomeEvent

    data class ShowMessage(val message: String) : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val restRepository: RestRepository,
    private val expenseRepository: ExpenseRepository,
    private val challengeRepository: ChallengeRepository,
    authRepository: AuthRepository
) : ViewModel() {

    val restState: StateFlow<RestState> = restRepository.restState

    val challengeState: StateFlow<ChallengeState> = challengeRepository.state

    val currentUser: StateFlow<User> = authRepository.currentUser

    val records: StateFlow<Map<String, ExpenseRecord>> = expenseRepository.records

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch { restRepository.syncStatus() }
        viewModelScope.launch {
            challengeRepository.loadCurrentChallenge().onFailure { error ->
                _events.send(HomeEvent.ShowMessage(error.toUserMessage("챌린지 현황 조회에 실패했습니다.")))
            }
        }
    }

    fun acknowledgeChallengeEnd() {
        viewModelScope.launch {
            challengeRepository.acknowledgeChallengeEnd().onFailure { error ->
                _events.send(HomeEvent.ShowMessage(error.toUserMessage("챌린지 종료 처리에 실패했습니다.")))
            }
            _events.send(HomeEvent.ChallengeEndAcknowledged)
        }
    }

    fun markNoRecord(date: LocalDate) = challengeRepository.markNoRecord(date)

    fun resumeNow() {
        viewModelScope.launch {
            restRepository.resumeNow()
                .onSuccess { _events.send(HomeEvent.ResumedFromBreak) }
                .onFailure { _events.send(HomeEvent.ShowMessage("휴식 복귀에 실패했습니다.")) }
        }
    }

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

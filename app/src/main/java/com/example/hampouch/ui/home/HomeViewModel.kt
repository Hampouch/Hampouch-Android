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
import com.example.hampouch.ui.common.LoadState
import com.example.hampouch.ui.widget.HomeWidgetStatePublisher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface HomeEvent {
    data object ResumedFromBreak : HomeEvent

    data object ChallengeEndAcknowledged : HomeEvent

    data object FixedDateChallengeDue : HomeEvent

    data class ShowMessage(val message: String) : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val restRepository: RestRepository,
    private val expenseRepository: ExpenseRepository,
    private val challengeRepository: ChallengeRepository,
    private val homeWidgetStatePublisher: HomeWidgetStatePublisher,
    authRepository: AuthRepository
) : ViewModel() {

    val restState: StateFlow<RestState> = restRepository.restState

    val challengeState: StateFlow<ChallengeState> = challengeRepository.state

    val currentUser: StateFlow<User> = authRepository.currentUser

    val records: StateFlow<Map<String, ExpenseRecord>> = expenseRepository.records

    /** 지출이 있거나 "오늘은 안 썼어요"를 누른 날짜들. */
    val daysWithRecord: StateFlow<Set<LocalDate>> = expenseRepository.daysWithRecord

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Loading)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()
    private var retryAction: (() -> Unit)? = null

    fun retry() = retryAction?.invoke()

    init {
        viewModelScope.launch { homeWidgetStatePublisher.publishSessionStarted() }
        viewModelScope.launch { restRepository.syncStatus() }
        loadCurrentChallenge()
    }

    private fun loadCurrentChallenge() {
        retryAction = ::loadCurrentChallenge
        _loadState.value = LoadState.Loading
        viewModelScope.launch {
            challengeRepository.loadCurrentChallenge()
                .onSuccess {
                    _loadState.value = LoadState.Content(challengeState.value.activeChallenge == null)
                    homeWidgetStatePublisher.publishAfterHomeSync(forceWidgetUpdate = true)
                    challengeRepository.loadFixedDateDraft().onSuccess { draft ->
                        val hasUnacknowledgedEnd = challengeState.value.isChallengeJustEnded(LocalDate.now())
                        if (draft?.isDue == true && !hasUnacknowledgedEnd) {
                            _events.send(HomeEvent.FixedDateChallengeDue)
                        }
                    }.onFailure { error ->
                        _events.send(
                            HomeEvent.ShowMessage(
                                error.toUserMessage("다음 챌린지 정보를 불러오지 못했습니다.")
                            )
                        )
                    }
                }
                .onFailure { error ->
                    val message = error.toUserMessage("챌린지 현황 조회에 실패했습니다.")
                    _loadState.value = LoadState.Failure(message)
                    _events.send(HomeEvent.ShowMessage(message))
                }
        }
    }

    fun acknowledgeChallengeEnd() {
        viewModelScope.launch {
            challengeRepository.acknowledgeChallengeEnd()
                .onSuccess { _events.send(HomeEvent.ChallengeEndAcknowledged) }
                .onFailure { error ->
                    _events.send(HomeEvent.ShowMessage(error.toUserMessage("챌린지 종료 처리에 실패했습니다.")))
                }
        }
    }

    fun markNoRecord(date: LocalDate) = challengeRepository.markNoRecord(date)

    fun resumeNow() {
        viewModelScope.launch {
            restRepository.resumeNow()
                .onSuccess { _events.send(HomeEvent.ResumedFromBreak) }
                .onFailure { error ->
                    _events.send(HomeEvent.ShowMessage(error.toUserMessage("휴식 복귀에 실패했습니다.")))
                }
        }
    }

    fun loadDay(date: LocalDate) {
        retryAction = { loadDay(date) }
        _loadState.value = LoadState.Loading
        viewModelScope.launch {
            expenseRepository.loadDay(date)
                .onSuccess {
                    _loadState.value = LoadState.Content(expenseRepository.recordsForDate(date).isEmpty())
                    if (date == LocalDate.now()) {
                        homeWidgetStatePublisher.publishAfterHomeSync(forceWidgetUpdate = true)
                    }
                }
                .onFailure { error ->
                    val message = error.toUserMessage("지출 내역 조회에 실패했습니다.")
                    _loadState.value = LoadState.Failure(message)
                    _events.send(HomeEvent.ShowMessage(message))
                }
        }
    }

    fun markNoSpending(date: LocalDate) {
        viewModelScope.launch {
            expenseRepository.markNoSpend(date)
                .onSuccess { homeWidgetStatePublisher.refreshAfterExpenseChange() }
                .onFailure { error ->
                    _events.send(
                        HomeEvent.ShowMessage(error.toUserMessage("오늘은 안 썼어요 기록에 실패했습니다."))
                    )
                }
        }
    }

    fun postponeOneDay() {
        viewModelScope.launch {
            restRepository.postponeOneDay()
                .onFailure { error ->
                    _events.send(HomeEvent.ShowMessage(error.toUserMessage("복귀 연기에 실패했습니다.")))
                }
        }
    }
}

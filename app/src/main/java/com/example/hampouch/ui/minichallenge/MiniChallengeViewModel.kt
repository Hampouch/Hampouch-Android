package com.example.hampouch.ui.minichallenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.MiniChallengeState
import com.example.hampouch.domain.model.MiniChallengeDuration
import com.example.hampouch.domain.model.RecommendedMiniChallenge
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.MiniChallengeRepository
import com.example.hampouch.ui.common.LoadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface MiniChallengeEvent {
    /** 추가가 반영된 날짜. 서버는 항상 오늘부터 만들기 때문에 선택 탭을 이 날짜로 옮겨야 한다. */
    data class Added(val date: LocalDate) : MiniChallengeEvent

    data class ShowMessage(val message: String) : MiniChallengeEvent
}

@HiltViewModel
class MiniChallengeViewModel @Inject constructor(
    private val miniChallengeRepository: MiniChallengeRepository
) : ViewModel() {

    val state: StateFlow<MiniChallengeState> = miniChallengeRepository.state

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()
    private var retryAction: (() -> Unit)? = null

    fun retry() = retryAction?.invoke()

    private val _events = Channel<MiniChallengeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun loadChallenges(date: LocalDate) {
        retryAction = { loadChallenges(date) }
        _loadState.value = LoadState.Loading
        viewModelScope.launch {
            miniChallengeRepository.loadChallenges(date)
                .onSuccess { _loadState.value = LoadState.Content(state.value.challengesFor(date).isEmpty()) }
                .onFailure {
                    val message = it.toUserMessage("미니 챌린지 조회에 실패했습니다.")
                    _loadState.value = LoadState.Failure(message)
                    _events.send(MiniChallengeEvent.ShowMessage(message))
                }
        }
    }

    fun loadRecommended(durationDays: Int? = null) {
        retryAction = { loadRecommended(durationDays) }
        _loadState.value = LoadState.Loading
        viewModelScope.launch {
            miniChallengeRepository.loadRecommended(durationDays)
                .onSuccess { _loadState.value = LoadState.Content(state.value.recommendedChallenges.isEmpty()) }
                .onFailure {
                    val message = it.toUserMessage("추천 목록 조회에 실패했습니다.")
                    _loadState.value = LoadState.Failure(message)
                    _events.send(MiniChallengeEvent.ShowMessage(message))
                }
        }
    }

    fun addRecommended(date: LocalDate, recommended: RecommendedMiniChallenge) {
        viewModelScope.launch {
            miniChallengeRepository.addRecommended(date, recommended)
                .onSuccess { _events.send(MiniChallengeEvent.Added(it)) }
                .onFailure { notify(it, "미니 챌린지 추가에 실패했습니다.") }
        }
    }

    fun addCustom(date: LocalDate, name: String, duration: MiniChallengeDuration) {
        viewModelScope.launch {
            miniChallengeRepository.addCustom(date, name, duration)
                .onSuccess { _events.send(MiniChallengeEvent.Added(it)) }
                .onFailure { notify(it, "미니 챌린지 추가에 실패했습니다.") }
        }
    }

    fun remove(date: LocalDate, id: String) {
        viewModelScope.launch {
            miniChallengeRepository.remove(date, id).onFailure { notify(it, "미니 챌린지 삭제에 실패했습니다.") }
        }
    }

    fun setChecked(date: LocalDate, id: String, checked: Boolean) {
        viewModelScope.launch {
            miniChallengeRepository.setChecked(date, id, checked)
                .onFailure { notify(it, "미니 챌린지 처리에 실패했습니다.") }
        }
    }

    private suspend fun notify(error: Throwable, fallback: String) {
        _events.send(MiniChallengeEvent.ShowMessage(error.toUserMessage(fallback)))
    }
}

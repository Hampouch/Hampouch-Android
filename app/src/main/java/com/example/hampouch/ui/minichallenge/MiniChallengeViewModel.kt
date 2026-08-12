package com.example.hampouch.ui.minichallenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.MiniChallengeState
import com.example.hampouch.domain.model.RecommendedMiniChallenge
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.MiniChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface MiniChallengeEvent {
    data class Added(val date: LocalDate) : MiniChallengeEvent

    data class ShowMessage(val message: String) : MiniChallengeEvent
}

@HiltViewModel
class MiniChallengeViewModel @Inject constructor(
    private val miniChallengeRepository: MiniChallengeRepository
) : ViewModel() {

    val state: StateFlow<MiniChallengeState> = miniChallengeRepository.state

    private val _events = Channel<MiniChallengeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun loadChallenges(date: LocalDate) {
        viewModelScope.launch {
            miniChallengeRepository.loadChallenges(date).onFailure { notify(it, "미니 챌린지 조회에 실패했습니다.") }
        }
    }

    fun loadRecommended(durationDays: Int? = null) {
        viewModelScope.launch {
            miniChallengeRepository.loadRecommended(durationDays).onFailure { notify(it, "추천 목록 조회에 실패했습니다.") }
        }
    }

    fun addRecommended(date: LocalDate, recommended: RecommendedMiniChallenge) {
        viewModelScope.launch {
            miniChallengeRepository.addRecommended(date, recommended)
                .onSuccess { added -> added?.let { _events.send(MiniChallengeEvent.Added(it)) } }
                .onFailure { notify(it, "미니 챌린지 추가에 실패했습니다.") }
        }
    }

    fun addCustom(date: LocalDate, name: String, totalDays: Int?) {
        viewModelScope.launch {
            miniChallengeRepository.addCustom(date, name, totalDays)
                .onSuccess { added -> added?.let { _events.send(MiniChallengeEvent.Added(it)) } }
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

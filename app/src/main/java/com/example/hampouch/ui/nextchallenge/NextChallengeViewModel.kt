package com.example.hampouch.ui.nextchallenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.OnboardingRequest
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.ChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NextChallengeEvent {
    data object Started : NextChallengeEvent
    data class ShowMessage(val message: String) : NextChallengeEvent
}

/** 다음 챌린지 시작 화면(일반·휴식 후) 공용. */
@HiltViewModel
class NextChallengeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository
) : ViewModel() {

    private val _isStarting = MutableStateFlow(false)
    val isStarting: StateFlow<Boolean> = _isStarting.asStateFlow()

    private val _events = Channel<NextChallengeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun startNewChallenge(request: OnboardingRequest) {
        if (_isStarting.value) return
        _isStarting.value = true
        viewModelScope.launch {
            challengeRepository.startNewChallenge(request)
                .onSuccess { _events.send(NextChallengeEvent.Started) }
                .onFailure { error ->
                    _events.send(
                        NextChallengeEvent.ShowMessage(error.toUserMessage("챌린지 시작에 실패했습니다."))
                    )
                }
            _isStarting.value = false
        }
    }
}

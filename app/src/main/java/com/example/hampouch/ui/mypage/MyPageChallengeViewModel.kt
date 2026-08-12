package com.example.hampouch.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.ChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageChallengeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository
) : ViewModel() {

    val challengeState: StateFlow<ChallengeState> = challengeRepository.state

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    fun loadHistory() {
        viewModelScope.launch {
            challengeRepository.loadHistory().onFailure { error ->
                _messages.send(error.toUserMessage("지난 챌린지 목록을 불러오지 못했습니다."))
            }
        }
    }

    fun loadResult(challengeId: String) {
        viewModelScope.launch {
            challengeRepository.loadResult(challengeId).onFailure { error ->
                _messages.send(error.toUserMessage("챌린지 결과를 불러오지 못했습니다."))
            }
        }
    }
}

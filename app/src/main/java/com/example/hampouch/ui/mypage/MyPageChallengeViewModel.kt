package com.example.hampouch.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.ui.common.LoadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()
    private var retryAction: (() -> Unit)? = null

    fun retry() = retryAction?.invoke()

    fun loadHistory() {
        retryAction = ::loadHistory
        _loadState.value = LoadState.Loading
        viewModelScope.launch {
            challengeRepository.loadHistory()
                .onSuccess { _loadState.value = LoadState.Content(challengeState.value.challenges.isEmpty()) }
                .onFailure { error ->
                    val message = error.toUserMessage("지난 챌린지 목록을 불러오지 못했습니다.")
                    _loadState.value = LoadState.Failure(message)
                    _messages.send(message)
                }
        }
    }

    fun loadResult(challengeId: String) {
        retryAction = { loadResult(challengeId) }
        _loadState.value = LoadState.Loading
        viewModelScope.launch {
            challengeRepository.loadResult(challengeId)
                .onSuccess { _loadState.value = LoadState.Content(false) }
                .onFailure { error ->
                    val message = error.toUserMessage("챌린지 결과를 불러오지 못했습니다.")
                    _loadState.value = LoadState.Failure(message)
                    _messages.send(message)
                }
        }
    }
}

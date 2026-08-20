package com.example.hampouch.ui.nextchallenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.ApiException
import com.example.hampouch.domain.model.OnboardingRequest
import com.example.hampouch.domain.model.FixedDateChallengeDraft
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.ui.widget.HomeWidgetStatePublisher
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

@HiltViewModel
class NextChallengeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val homeWidgetStatePublisher: HomeWidgetStatePublisher
) : ViewModel() {

    private val _isStarting = MutableStateFlow(false)
    val isStarting: StateFlow<Boolean> = _isStarting.asStateFlow()

    private val _recommendationMessage = MutableStateFlow<String?>(null)
    val recommendationMessage: StateFlow<String?> = _recommendationMessage.asStateFlow()

    private val _events = Channel<NextChallengeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun loadRecommendation() {
        viewModelScope.launch {
            challengeRepository.loadRecommendation()
                .onSuccess { _recommendationMessage.value = it }
        }
    }

    fun startNewChallenge(request: OnboardingRequest) {
        if (_isStarting.value) return
        _isStarting.value = true
        viewModelScope.launch {
            challengeRepository.startNewChallenge(request)
                .onSuccess {
                    homeWidgetStatePublisher.publishAfterHomeSync()
                    _events.send(NextChallengeEvent.Started)
                }
                .onFailure { error ->
                    _events.send(
                        NextChallengeEvent.ShowMessage(error.toUserMessage("챌린지 시작에 실패했습니다."))
                    )
                }
            _isStarting.value = false
        }
    }

    fun startFixedDateChallenge(
        draft: FixedDateChallengeDraft,
        startDate: java.time.LocalDate
    ) {
        if (_isStarting.value) return
        _isStarting.value = true
        viewModelScope.launch {
            attemptStartFixedDateChallenge(draft.sourceChallengeId, startDate, allowRetryOnStale = true)
            _isStarting.value = false
        }
    }

    private suspend fun attemptStartFixedDateChallenge(
        sourceChallengeId: Long,
        startDate: java.time.LocalDate,
        allowRetryOnStale: Boolean
    ) {
        challengeRepository.startFixedDateChallenge(
            sourceChallengeId = sourceChallengeId,
            startDate = startDate
        ).onSuccess {
            homeWidgetStatePublisher.publishAfterHomeSync()
            _events.send(NextChallengeEvent.Started)
        }.onFailure { error ->
            val isStale = allowRetryOnStale && (error as? ApiException)?.code == "FIXED_DATE_SOURCE_STALE"
            val freshSourceChallengeId = if (isStale) {
                challengeRepository.loadFixedDateDraft().getOrNull()?.takeIf { it.isDue }?.sourceChallengeId
            } else {
                null
            }
            if (freshSourceChallengeId != null) {
                attemptStartFixedDateChallenge(freshSourceChallengeId, startDate, allowRetryOnStale = false)
            } else {
                _events.send(
                    NextChallengeEvent.ShowMessage(error.toUserMessage("챌린지 시작에 실패했습니다."))
                )
            }
        }
    }
}

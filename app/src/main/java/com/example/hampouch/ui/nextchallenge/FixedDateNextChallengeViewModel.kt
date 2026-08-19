package com.example.hampouch.ui.nextchallenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.FixedDateChallengeDraft
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.ChallengeRepository
import com.example.hampouch.ui.widget.HomeWidgetStatePublisher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface FixedDateNextChallengeEvent {
    data object Started : FixedDateNextChallengeEvent
    data object Unavailable : FixedDateNextChallengeEvent
    data class ShowMessage(val message: String) : FixedDateNextChallengeEvent
}

@HiltViewModel
class FixedDateNextChallengeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val homeWidgetStatePublisher: HomeWidgetStatePublisher
) : ViewModel() {

    val draft: StateFlow<FixedDateChallengeDraft?> = challengeRepository.fixedDateDraft

    private val _events = Channel<FixedDateNextChallengeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var isStarting = false

    fun loadDraft() {
        viewModelScope.launch {
            challengeRepository.loadFixedDateDraft()
                .onSuccess { draft ->
                    if (draft?.isDue != true) _events.send(FixedDateNextChallengeEvent.Unavailable)
                }
                .onFailure { error ->
                    _events.send(
                        FixedDateNextChallengeEvent.ShowMessage(
                            error.toUserMessage("다음 챌린지 정보를 불러오지 못했습니다.")
                        )
                    )
                }
        }
    }

    fun startChallenge(draft: FixedDateChallengeDraft) {
        if (isStarting) return
        isStarting = true
        viewModelScope.launch {
            challengeRepository.startFixedDateChallenge(
                sourceChallengeId = draft.sourceChallengeId,
                startDate = draft.nextStartDate,
                budgetTotal = draft.budgetTotal,
                fixedDay = draft.fixedDay
            ).onSuccess {
                homeWidgetStatePublisher.publishAfterHomeSync()
                _events.send(FixedDateNextChallengeEvent.Started)
            }.onFailure { error ->
                _events.send(
                    FixedDateNextChallengeEvent.ShowMessage(
                        error.toUserMessage("다음 챌린지 시작에 실패했습니다.")
                    )
                )
            }
            isStarting = false
        }
    }
}

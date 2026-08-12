package com.example.hampouch.ui.hambattle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.data.local.HamBattleMockStore
import com.example.hampouch.domain.model.BattleState
import com.example.hampouch.domain.model.HamBattleChallenge
import com.example.hampouch.domain.model.HamBattleStatus
import com.example.hampouch.domain.model.HamBattleChallengeRequest
import com.example.hampouch.domain.model.toUserMessage
import com.example.hampouch.domain.repository.BattleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface HamBattleEvent {
    data object Created : HamBattleEvent

    data class Joined(val battleId: Long) : HamBattleEvent

    data class ShowMessage(val message: String) : HamBattleEvent
}

@HiltViewModel
class HamBattleViewModel @Inject constructor(
    private val battleRepository: BattleRepository,
    private val mockStore: HamBattleMockStore
) : ViewModel() {

    val state: StateFlow<BattleState> = battleRepository.state

    val mockChallenges: StateFlow<List<HamBattleChallenge>> = mockStore.challenges

    fun mockChallengesWith(status: HamBattleStatus, referenceToday: LocalDate = LocalDate.now()) =
        mockChallenges.value.filter { it.status(referenceToday) == status }

    fun myMissedStreakDays(challenge: HamBattleChallenge) = mockStore.myMissedStreakDays(challenge)
    fun disqualifyMe(challengeId: String) = mockStore.disqualifyMe(challengeId)
    fun cancelChallenge(challengeId: String) = mockStore.cancelChallenge(challengeId)
    fun isCancellationAcknowledged(id: String) = mockStore.isCancellationAcknowledged(id)
    fun acknowledgeCancellation(id: String) = mockStore.acknowledgeCancellation(id)
    fun isDisqualificationAcknowledged(id: String) = mockStore.isDisqualificationAcknowledged(id)
    fun acknowledgeDisqualification(id: String) = mockStore.acknowledgeDisqualification(id)
    fun wasMissedWarningShownToday(id: String) = mockStore.wasMissedWarningShownToday(id)
    fun markMissedWarningShown(id: String) = mockStore.markMissedWarningShown(id)
    fun removeWaitingChallenge(id: String) = mockStore.removeWaitingChallenge(id)
    fun participantsForToday(challenge: HamBattleChallenge) = mockStore.participantsForToday(challenge)

    fun joinChallengeFromCommunityPost(
        authorName: String,
        title: String,
        penalty: String,
        battleCode: String,
        totalCount: Int
    ): HamBattleChallenge? = mockStore.joinChallengeFromCommunityPost(
        authorName = authorName,
        title = title,
        penalty = penalty,
        battleCode = battleCode,
        totalCount = totalCount
    )

    private val _events = Channel<HamBattleEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun loadMyBattles() {
        viewModelScope.launch {
            battleRepository.loadMyBattles().onFailure { notify(it, "햄배틀 목록 조회에 실패했습니다.") }
        }
    }

    fun loadBattleDetail(battleId: Long) {
        viewModelScope.launch {
            battleRepository.loadBattleDetail(battleId).onFailure { notify(it, "햄배틀 상세 조회에 실패했습니다.") }
        }
    }

    fun create(request: HamBattleChallengeRequest) {
        viewModelScope.launch {
            battleRepository.create(request)
                .onSuccess { _events.send(HamBattleEvent.Created) }
                .onFailure { notify(it, "햄배틀 생성에 실패했습니다.") }
        }
    }

    fun join(battleCode: String) {
        viewModelScope.launch {
            battleRepository.join(battleCode)
                .onSuccess { _events.send(HamBattleEvent.Joined(it)) }
                .onFailure { notify(it, "햄배틀 참가에 실패했습니다.") }
        }
    }

    private suspend fun notify(error: Throwable, fallback: String) {
        _events.send(HamBattleEvent.ShowMessage(error.toUserMessage(fallback)))
    }
}

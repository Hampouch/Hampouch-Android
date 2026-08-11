package com.example.hampouch.ui.common

import androidx.lifecycle.ViewModel
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.repository.ChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * 챌린지 상태를 읽고 간단한 플래그만 바꾸는 화면/라우트가 쓰는 얇은 ViewModel.
 *
 * 내비게이션 그래프에서 "진행 중 챌린지 id로 목적지를 정하는" 정도의 용도다.
 */
@HiltViewModel
class ChallengeLookupViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository
) : ViewModel() {

    val challengeState: StateFlow<ChallengeState> = challengeRepository.state

    fun markVisitedExpenseEditAfterEnd() = challengeRepository.markVisitedExpenseEditAfterEnd()
}

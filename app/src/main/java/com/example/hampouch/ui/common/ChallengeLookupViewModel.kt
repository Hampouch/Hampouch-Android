package com.example.hampouch.ui.common

import androidx.lifecycle.ViewModel
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.repository.ChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ChallengeLookupViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository
) : ViewModel() {

    val challengeState: StateFlow<ChallengeState> = challengeRepository.state

    fun markVisitedExpenseEditAfterEnd() = challengeRepository.markVisitedExpenseEditAfterEnd()
}

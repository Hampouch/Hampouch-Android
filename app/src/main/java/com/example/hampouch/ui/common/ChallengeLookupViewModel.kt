package com.example.hampouch.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.FixedDateChallengeDraft
import com.example.hampouch.domain.repository.ChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengeLookupViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository
) : ViewModel() {

    val challengeState: StateFlow<ChallengeState> = challengeRepository.state
    val fixedDateDraft: StateFlow<FixedDateChallengeDraft?> = challengeRepository.fixedDateDraft

    fun loadFixedDateDraft() {
        viewModelScope.launch { challengeRepository.loadFixedDateDraft() }
    }

    fun markVisitedExpenseEditAfterEnd() = challengeRepository.markVisitedExpenseEditAfterEnd()
}

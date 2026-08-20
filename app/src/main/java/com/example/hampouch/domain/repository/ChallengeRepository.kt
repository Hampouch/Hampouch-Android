package com.example.hampouch.domain.repository

import com.example.hampouch.domain.model.ActiveChallenge
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.FixedDateChallengeDraft
import com.example.hampouch.domain.model.OnboardingRequest
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

interface ChallengeRepository {

    val state: StateFlow<ChallengeState>

    val fixedDateDraft: StateFlow<FixedDateChallengeDraft?>

    suspend fun loadCurrentChallenge(): Result<Unit>

    suspend fun loadHistory(): Result<Unit>

    suspend fun loadResult(challengeId: String): Result<Unit>

    suspend fun updateFocusCategories(categories: List<String>): Result<List<String>>

    suspend fun startNewChallenge(
        request: OnboardingRequest,
        referenceToday: LocalDate = LocalDate.now()
    ): Result<ActiveChallenge>

    suspend fun abandonChallenge(referenceToday: LocalDate = LocalDate.now()): Result<Unit>

    suspend fun acknowledgeChallengeEnd(): Result<Unit>

    suspend fun loadFixedDateDraft(): Result<FixedDateChallengeDraft?>

    suspend fun startFixedDateChallenge(
        sourceChallengeId: Long,
        startDate: LocalDate
    ): Result<ActiveChallenge>

    suspend fun updateTargetAmount(newTargetAmount: Int, effectiveFrom: LocalDate = LocalDate.now()): Result<Unit>

    suspend fun loadRecommendation(): Result<String>

    fun markNoRecord(date: LocalDate)

    fun clearNoRecord(date: LocalDate)

    fun markVisitedExpenseEditAfterEnd()

    fun resetForAccount(referenceToday: LocalDate = LocalDate.now())

    fun resetEmpty()
}

package com.example.hampouch.domain.model

import java.time.LocalDate

enum class FixedDateDraftState {
    WAITING,
    DUE
}

data class FixedDateChallengeDraft(
    val state: FixedDateDraftState,
    val sourceChallengeId: Long,
    val previousStartDate: LocalDate,
    val previousEndDate: LocalDate,
    val fixedDay: Int,
    val nextStartDate: LocalDate,
    val nextEndDate: LocalDate,
    val durationDays: Int,
    val budgetTotal: Int,
    val dailyLimit: Int
) {
    val isDue: Boolean get() = state == FixedDateDraftState.DUE
}

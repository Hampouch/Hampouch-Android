package com.example.hampouch.ui.common

import com.example.hampouch.domain.model.ActiveChallenge
import com.example.hampouch.domain.model.ChallengeState
import java.time.LocalDate

fun previewChallengeState(referenceToday: LocalDate = LocalDate.now()): ChallengeState {
    val periodStart = referenceToday.minusDays(6)
    return ChallengeState(
        challenges = listOf(
            ActiveChallenge(
                id = "preview_challenge",
                totalDays = 14,
                periodStart = periodStart,
                periodEnd = periodStart.plusDays(13),
                dailyLimit = 20_000,
                targetAmount = 280_000,
                savedAmount = 21_400,
                streakDays = 4,
                editCount = 0
            )
        )
    )
}

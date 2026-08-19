package com.example.hampouch.ui.amountadjustment

import com.example.hampouch.domain.model.ActiveChallenge
import com.example.hampouch.domain.model.ChallengeState
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmountAdjustmentViewModelTest {

    @Test
    fun `포기 성공 후 activeChallenge가 없어도 id로 결과 챌린지를 찾는다`() {
        val today = LocalDate.of(2026, 8, 18)
        val abandoned = ActiveChallenge(
            id = "42",
            totalDays = 7,
            periodStart = today,
            periodEnd = today.plusDays(6),
            dailyLimit = 10_000,
            targetAmount = 70_000,
            savedAmount = 0,
            streakDays = 0,
            editCount = 0,
            abandonedDate = today,
            remoteStatus = "FAIL"
        )
        val state = ChallengeState(challenges = listOf(abandoned))

        assertNull(state.activeChallenge)
        assertEquals(abandoned, state.challengeForAbandonResult("42"))
    }
}

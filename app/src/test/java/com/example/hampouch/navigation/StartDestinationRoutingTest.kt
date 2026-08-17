package com.example.hampouch.navigation

import com.example.hampouch.domain.model.PendingChallengeResult
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StartDestinationRoutingTest {

    @Test
    fun `같은 계정의 미확인 실패 결과는 잠긴 결과 화면으로 복원한다`() {
        val pending = pending(userId = 7L)

        val route = pendingChallengeResultRoute(userId = 7L, pending = pending)

        assertEquals(Screen.ChallengeSummary.createRoute("42", locked = true), route)
    }

    @Test
    fun `다른 계정의 실패 결과는 복원하지 않는다`() {
        val route = pendingChallengeResultRoute(userId = 8L, pending = pending(userId = 7L))

        assertNull(route)
    }

    private fun pending(userId: Long) = PendingChallengeResult(
        userId = userId,
        challengeId = "42",
        title = "14일 챌린지",
        periodStart = LocalDate.of(2026, 8, 1),
        periodEnd = LocalDate.of(2026, 8, 14),
        totalDays = 14,
        successDays = 5,
        streakDays = 3,
        amountValue = 20_000,
        goalAmount = 300_000,
        actualAmount = 320_000,
        dailyLimit = 21_428,
        emotionStats = emptyList(),
        dailyRecords = emptyMap()
    )
}

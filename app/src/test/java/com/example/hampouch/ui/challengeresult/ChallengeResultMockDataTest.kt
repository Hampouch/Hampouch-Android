package com.example.hampouch.ui.challengeresult

import com.example.hampouch.domain.model.ActiveChallenge
import com.example.hampouch.domain.model.ChallengeResultStatus
import com.example.hampouch.domain.model.ChallengeState
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ChallengeResultMockDataTest {

    private fun oneDayChallenge(today: LocalDate, remoteStatus: String? = null) = ActiveChallenge(
        id = "c1",
        totalDays = 1,
        periodStart = today,
        periodEnd = today,
        dailyLimit = 10_000,
        targetAmount = 10_000,
        savedAmount = 0,
        streakDays = 0,
        editCount = 0,
        remoteStatus = remoteStatus
    )

    @Test
    fun `종료일 당일에는 서버가 SUCCESS를 내려줘도 진행중으로 표시된다`() {
        val today = LocalDate.of(2026, 8, 16)
        val challenge = oneDayChallenge(today, remoteStatus = "SUCCESS")
        val state = ChallengeState(challenges = listOf(challenge))

        val result = ChallengeResultMockData.forChallenge(
            challenge = challenge,
            challengeState = state,
            recordsForDate = { emptyList() },
            hasRecordOnDate = { false },
            referenceToday = today
        )

        assertEquals(ChallengeResultStatus.IN_PROGRESS, result.status)
    }

    @Test
    fun `종료일 다음날부터는 remoteStatus에 따라 성공 실패로 확정된다`() {
        val today = LocalDate.of(2026, 8, 16)
        val challenge = oneDayChallenge(today, remoteStatus = "SUCCESS")
        val state = ChallengeState(challenges = listOf(challenge))

        val result = ChallengeResultMockData.forChallenge(
            challenge = challenge,
            challengeState = state,
            recordsForDate = { emptyList() },
            hasRecordOnDate = { false },
            referenceToday = today.plusDays(1)
        )

        assertEquals(ChallengeResultStatus.COMPLETE, result.status)
    }

    @Test
    fun `같은 날 시작한 포기된 챌린지와 새 챌린지가 섞여도 각자 올바른 상태로 표시된다`() {
        val today = LocalDate.of(2026, 8, 16)
        val abandoned = ActiveChallenge(
            id = "old",
            totalDays = 7,
            periodStart = today,
            periodEnd = today.plusDays(6),
            dailyLimit = 10_000,
            targetAmount = 70_000,
            savedAmount = 0,
            streakDays = 0,
            editCount = 0,
            abandonedDate = today,
            remoteStatus = "GIVEN_UP"
        )
        val active = ActiveChallenge(
            id = "new",
            totalDays = 1,
            periodStart = today,
            periodEnd = today,
            dailyLimit = 10_000,
            targetAmount = 10_000,
            savedAmount = 0,
            streakDays = 0,
            editCount = 0
        )
        val state = ChallengeState(challenges = listOf(abandoned, active))

        val abandonedResult = ChallengeResultMockData.forChallenge(
            challenge = abandoned,
            challengeState = state,
            recordsForDate = { emptyList() },
            hasRecordOnDate = { false },
            referenceToday = today
        )
        val activeResult = ChallengeResultMockData.forChallenge(
            challenge = active,
            challengeState = state,
            recordsForDate = { emptyList() },
            hasRecordOnDate = { false },
            referenceToday = today
        )

        assertEquals(ChallengeResultStatus.FAIL, abandonedResult.status)
        assertEquals(ChallengeResultStatus.IN_PROGRESS, activeResult.status)
    }

    @Test
    fun `세션이 바뀌어 로컬 포기 기록이 없어도 서버가 FAIL을 내려주면 실패로 표시된다`() {
        val today = LocalDate.of(2026, 8, 16)
        val challenge = ActiveChallenge(
            id = "old",
            totalDays = 7,
            periodStart = today,
            periodEnd = today.plusDays(6),
            dailyLimit = 10_000,
            targetAmount = 70_000,
            savedAmount = 0,
            streakDays = 0,
            editCount = 0,
            abandonedDate = null,
            remoteStatus = "FAIL"
        )
        val state = ChallengeState(challenges = listOf(challenge))

        val result = ChallengeResultMockData.forChallenge(
            challenge = challenge,
            challengeState = state,
            recordsForDate = { emptyList() },
            hasRecordOnDate = { false },
            referenceToday = today
        )

        assertEquals(ChallengeResultStatus.FAIL, result.status)
    }

    @Test
    fun `서버 기간이 0이면 시작일과 종료일로 결과 기간을 복구한다`() {
        val startDate = LocalDate.of(2026, 8, 17)
        val challenge = ActiveChallenge(
            id = "zero-duration",
            totalDays = 0,
            periodStart = startDate,
            periodEnd = startDate.plusDays(6),
            dailyLimit = 7_142,
            targetAmount = 50_000,
            savedAmount = 0,
            streakDays = 0,
            editCount = 0,
            abandonedDate = startDate,
            remoteStatus = "FAIL"
        )

        val result = ChallengeResultMockData.forChallenge(
            challenge = challenge,
            challengeState = ChallengeState(challenges = listOf(challenge)),
            recordsForDate = { emptyList() },
            hasRecordOnDate = { false },
            referenceToday = startDate
        )

        assertEquals(7, result.totalDays)
        assertEquals("7일 챌린지", result.title)
    }
}

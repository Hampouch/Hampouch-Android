package com.example.hampouch.domain.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ChallengeStateTest {

    private fun challengeStartingToday(today: LocalDate) = ActiveChallenge(
        id = "c1",
        totalDays = 7,
        periodStart = today,
        periodEnd = today.plusDays(6),
        dailyLimit = 10_000,
        targetAmount = 70_000,
        savedAmount = 0,
        streakDays = 0,
        editCount = 0
    )

    @Test
    fun `아직 아무 입력도 없는 오늘은 성공한 날로 세지 않는다`() {
        val today = LocalDate.of(2026, 8, 16)
        val challenge = challengeStartingToday(today)
        val state = ChallengeState(challenges = listOf(challenge))

        val progress = state.computeProgress(
            referenceToday = today,
            challenge = challenge,
            hasRecordOnDate = { false },
            spentOnDate = { 0 }
        )

        assertEquals(0, progress.dailyRecords.values.count { it == DailyRecordStatus.SUCCESS })
        assertEquals(0, progress.streakDays)
    }

    @Test
    fun `오늘 지출 없음으로 명시적으로 기록하면 성공한 날로 센다`() {
        val today = LocalDate.of(2026, 8, 16)
        val challenge = challengeStartingToday(today)
        val state = ChallengeState(challenges = listOf(challenge))

        val progress = state.computeProgress(
            referenceToday = today,
            challenge = challenge,
            hasRecordOnDate = { true },
            spentOnDate = { 0 }
        )

        assertEquals(1, progress.dailyRecords.values.count { it == DailyRecordStatus.SUCCESS })
        assertEquals(1, progress.streakDays)
    }

    @Test
    fun `지출을 입력했지만 한도를 초과하면 성공한 날이 아니다`() {
        val today = LocalDate.of(2026, 8, 16)
        val challenge = challengeStartingToday(today)
        val state = ChallengeState(challenges = listOf(challenge))

        val progress = state.computeProgress(
            referenceToday = today,
            challenge = challenge,
            hasRecordOnDate = { true },
            spentOnDate = { 20_000 }
        )

        assertEquals(0, progress.dailyRecords.values.count { it == DailyRecordStatus.SUCCESS })
        assertEquals(0, progress.streakDays)
    }
}

package com.example.hampouch.ui.home

import com.example.hampouch.domain.model.ChallengeProgress
import com.example.hampouch.domain.model.HomeChallenge
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeChallengeProgressTest {

    private val serverChallenge = HomeChallenge(
        totalDays = 100,
        dDay = 99,
        periodStartLabel = "8월 17일",
        periodEndLabel = "11월 24일",
        dailyLimit = 3_000,
        todayBalance = -9_000,
        savedAmount = -9_000,
        streakDays = 1
    )

    private val localProgress = ChallengeProgress(
        savedAmount = 0,
        streakDays = 0,
        dailyRecords = emptyMap()
    )

    @Test
    fun `서버 모드에서는 절약액은 서버 값을 유지하고 연속 달성은 챌린지 상세와 동일하게 로컬로 계산한다`() {
        val resolved = resolveHomeChallengeProgress(
            challenge = serverChallenge,
            localProgress = localProgress,
            useServerChallenge = true
        )

        assertEquals(-9_000, resolved.savedAmount)
        assertEquals(0, resolved.streakDays)
    }

    @Test
    fun `목 모드에서는 로컬 계산 진행 현황을 사용한다`() {
        val resolved = resolveHomeChallengeProgress(
            challenge = serverChallenge,
            localProgress = localProgress,
            useServerChallenge = false
        )

        assertEquals(0, resolved.savedAmount)
        assertEquals(0, resolved.streakDays)
    }
}

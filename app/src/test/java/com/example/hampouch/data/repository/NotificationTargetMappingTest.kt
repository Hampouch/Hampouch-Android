package com.example.hampouch.data.repository

import com.example.hampouch.data.remote.dto.NotificationTargetDto
import com.example.hampouch.domain.model.NotificationTarget
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationTargetMappingTest {

    @Test
    fun `screen이 CHALLENGE_DETAIL이면 챌린지 요약으로 이동한다`() {
        val target = NotificationTargetDto(screen = SCREEN_CHALLENGE_DETAIL, challengeId = 42L).toDomain()

        assertEquals(NotificationTarget.ChallengeSummary("42"), target)
    }

    @Test
    fun `screen이 CHALLENGE_RESULT이면 챌린지 요약으로 이동한다`() {
        val target = NotificationTargetDto(screen = SCREEN_CHALLENGE_RESULT, challengeId = 7L).toDomain()

        assertEquals(NotificationTarget.ChallengeSummary("7"), target)
    }

    @Test
    fun `서버가 확정하지 않은 screen 값은 임의의 화면으로 연결하지 않고 홈으로 보낸다`() {
        val target = NotificationTargetDto(screen = "SOME_UNKNOWN_SCREEN", challengeId = 1L).toDomain()

        assertEquals(NotificationTarget.Home, target)
    }

    @Test
    fun `challengeId가 없으면 screen 값과 무관하게 홈으로 보낸다`() {
        val target = NotificationTargetDto(screen = SCREEN_CHALLENGE_DETAIL, challengeId = null).toDomain()

        assertEquals(NotificationTarget.Home, target)
    }

    @Test
    fun `target dto 자체가 null이면 홈으로 보낸다`() {
        val target: NotificationTargetDto? = null

        assertEquals(NotificationTarget.Home, target.toDomain())
    }
}

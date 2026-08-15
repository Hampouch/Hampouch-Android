package com.example.hampouch.data.repository

import com.example.hampouch.data.remote.dto.MiniChallengeCreatedData
import com.example.hampouch.domain.model.MiniChallengeDuration
import com.example.hampouch.domain.model.MiniChallengeEntry
import com.example.hampouch.domain.model.MiniChallengeState
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MiniChallengeContractMappingTest {

    @Test
    fun `생성 후 이동 날짜는 서버 startDate를 사용한다`() {
        val response = MiniChallengeCreatedData(
            miniChallengeId = 5L,
            title = "물 마시기",
            durationDays = 7,
            startDate = "2026-08-14",
            endDate = "2026-08-20"
        )

        assertEquals(LocalDate.of(2026, 8, 14), response.createdStartDate())
    }

    @Test
    fun `과거 화면이더라도 중복 검사는 서버 생성 날짜 목록을 기준으로 한다`() {
        val selectedDate = LocalDate.of(2026, 8, 13)
        val serverCreationDate = LocalDate.of(2026, 8, 14)
        val state = MiniChallengeState(
            challengesByDate = mapOf(
                serverCreationDate to listOf(
                    MiniChallengeEntry(
                        id = "5",
                        name = "편의점 디저트 안 먹기",
                        duration = MiniChallengeDuration.Period(7)
                    )
                )
            )
        )

        assertFalse(state.isNameTaken(selectedDate, "편의점 디저트 안 먹기"))
        assertTrue(state.isNameTaken(serverCreationDate, " 편의점디저트 안먹기 "))
    }

    @Test
    fun `공백 이름은 저장용 이름으로 변환하지 않는다`() {
        assertNull(validMiniChallengeNameOrNull("   "))
        assertEquals("물 마시기", validMiniChallengeNameOrNull("  물 마시기  "))
    }
}

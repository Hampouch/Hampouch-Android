package com.example.hampouch.data.repository

import com.example.hampouch.data.remote.dto.ChallengeCreateData
import com.google.gson.Gson
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ChallengeContractMappingTest {

    @Test
    fun `생성 응답은 durationDays 없이 역직렬화한다`() {
        val response = Gson().fromJson(
            """
            {
              "challengeId": 1,
              "dailyLimit": 3333,
              "startDate": "2026-08-17",
              "endDate": "2026-09-15",
              "status": "IN_PROGRESS"
            }
            """.trimIndent(),
            ChallengeCreateData::class.java
        )

        assertEquals(1L, response.challengeId)
        assertEquals("2026-09-15", response.endDate)
    }

    @Test
    fun `생성 응답의 시작일과 종료일로 전체 기간을 계산한다`() {
        val startDate = LocalDate.of(2026, 8, 17)
        val endDate = LocalDate.of(2026, 9, 15)

        assertEquals(30, challengeDurationDays(startDate, endDate))
    }

    @Test
    fun `시작일과 종료일이 같으면 1일 챌린지다`() {
        val date = LocalDate.of(2026, 8, 17)

        assertEquals(1, challengeDurationDays(date, date))
    }

    @Test
    fun `종료일이 시작일보다 빠르면 잘못된 서버 응답으로 처리한다`() {
        assertThrows(IllegalArgumentException::class.java) {
            challengeDurationDays(
                startDate = LocalDate.of(2026, 8, 18),
                endDate = LocalDate.of(2026, 8, 17)
            )
        }
    }
}

package com.example.hampouch.data.repository

import com.example.hampouch.data.remote.dto.MiniChallengeCreatedData
import java.time.LocalDate
import org.junit.Assert.assertEquals
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
}

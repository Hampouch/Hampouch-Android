package com.example.hampouch.data.repository

import com.example.hampouch.data.remote.dto.BattleDetailData
import com.example.hampouch.data.remote.dto.BattleParticipantDto
import com.example.hampouch.domain.model.HamBattleServerState
import com.example.hampouch.domain.model.HamBattleStatus
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleContractMappingTest {

    @Test
    fun `상세 응답의 int64 금액과 서버 순위를 보존한다`() {
        val detail = detail(
            status = "ONGOING",
            participants = listOf(
                BattleParticipantDto(
                    userId = 1L,
                    nickname = "사용자",
                    avatarUrl = null,
                    rank = 3,
                    todayAmount = 3_000_000_000L,
                    totalAmount = 4_000_000_000L,
                    isValid = true
                )
            )
        )

        val participant = detail.toDomain(myUserId = 2L).participants.single()

        assertEquals(3_000_000_000L, participant.todayAmount)
        assertEquals(4_000_000_000L, participant.amount)
        assertEquals(3, participant.rank)
    }

    @Test
    fun `CANCELLED 상세 상태를 계약 오류로 버리지 않는다`() {
        val challenge = detail(status = "CANCELLED").toDomain(myUserId = 1L)

        assertTrue(challenge.serverState is HamBattleServerState.Cancelled)
        assertTrue(challenge.cancelled)
        assertEquals(HamBattleStatus.ENDED, challenge.status(LocalDate.of(2026, 8, 15)))
    }

    private fun detail(
        status: String,
        participants: List<BattleParticipantDto> = emptyList()
    ) = BattleDetailData(
        battleId = 1L,
        battleCode = "ABC1234",
        title = "식비 절약",
        penalty = "커피 사기",
        status = status,
        startDate = "2026-08-15",
        endDate = "2026-08-21",
        participants = participants,
        penaltyTargetNickname = null
    )
}

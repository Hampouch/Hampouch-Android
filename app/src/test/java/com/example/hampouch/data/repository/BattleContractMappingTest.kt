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

    @Test
    fun `서버가 ONGOING으로 내려준 햄배틀은 오늘 날짜와 무관하게 진행 상태다`() {
        val challenge = detail(status = "ONGOING").toDomain(myUserId = 1L)

        assertEquals(HamBattleStatus.ACTIVE, challenge.status(LocalDate.of(2026, 8, 15)))
    }

    @Test
    fun `정원이 차고 시작일이 오늘이어도 서버가 READY면 대기 상태를 유지한다`() {
        val participants = listOf(
            participant(userId = 1L, nickname = "나"),
            participant(userId = 2L, nickname = "상대")
        )
        val challenge = detail(status = "READY", participants = participants).toDomain(myUserId = 1L)

        assertEquals(HamBattleStatus.WAITING, challenge.status(LocalDate.of(2026, 8, 15)))
    }

    @Test
    fun `프로필 사진 변경값은 내 참가자 avatarUrl을 덮어쓴다`() {
        val detail = detail(
            status = "ONGOING",
            participants = listOf(
                BattleParticipantDto(
                    userId = 1L,
                    nickname = "나의 닉네임",
                    avatarUrl = "https://old.example/avatar.jpg",
                    todayAmount = 0L,
                    totalAmount = 0L
                ),
                BattleParticipantDto(
                    userId = 2L,
                    nickname = "상대",
                    avatarUrl = "https://other.example/avatar.jpg",
                    todayAmount = 0L,
                    totalAmount = 0L
                )
            )
        )

        val participants = detail.toDomain(
            myUserId = 1L,
            hasMyAvatarOverride = true,
            myAvatarOverride = "https://new.example/avatar.jpg"
        ).participants

        assertEquals("https://new.example/avatar.jpg", participants[0].avatarUrl)
        assertEquals("https://other.example/avatar.jpg", participants[1].avatarUrl)
    }

    @Test
    fun `기본 프로필로 변경하면 내 참가자 avatarUrl을 null로 덮어쓴다`() {
        val detail = detail(
            status = "ONGOING",
            participants = listOf(
                BattleParticipantDto(
                    userId = 1L,
                    nickname = "나의 닉네임",
                    avatarUrl = "https://old.example/avatar.jpg",
                    todayAmount = 0L,
                    totalAmount = 0L
                )
            )
        )

        val participant = detail.toDomain(
            myUserId = 1L,
            hasMyAvatarOverride = true,
            myAvatarOverride = null
        ).participants.single()

        assertEquals(null, participant.avatarUrl)
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

    private fun participant(userId: Long, nickname: String) = BattleParticipantDto(
        userId = userId,
        nickname = nickname,
        avatarUrl = null,
        todayAmount = 0L,
        totalAmount = 0L
    )
}

package com.example.hampouch.data.local

import com.example.hampouch.domain.model.HamBattleChallenge
import com.example.hampouch.domain.model.HamBattleParticipantSpending
import com.example.hampouch.domain.model.HamBattleParticipantStatus
import com.example.hampouch.domain.model.HamBattleStatus
import java.time.LocalDate

// TODO: 서버팀 햄배틀 API 연동 시 목데이터 대신 실제 응답으로 대체.
object HamBattleMockFixtures {

    fun seedChallenges(): List<HamBattleChallenge> =
        listOf(
            HamBattleChallenge(
                id = "1",
                type = "1 vs 1",
                title = "이번주 식비 덜 쓰는 사람이 이긴다",
                penalty = "커피 사기",
                participants = listOf(
                    HamBattleParticipantSpending("나", 73_200),
                    HamBattleParticipantSpending("이준혁", 113_000)
                ),
                totalCount = 2,
                durationDays = 7,
                startDate = LocalDate.now().minusDays(3)
            ),
            HamBattleChallenge(
                id = "2",
                type = "그룹",
                title = "5월 식비 절약왕 가리기",
                penalty = "전원에게 삼겹살 쏘기",
                participants = listOf(
                    HamBattleParticipantSpending("김수진", 161_000),
                    HamBattleParticipantSpending("나", 73_200),
                    HamBattleParticipantSpending("박민서", 87_500),
                    HamBattleParticipantSpending("일이삼사오육칠팔구공", 0),
                    HamBattleParticipantSpending(
                        "이준혁",
                        113_000,
                        status = HamBattleParticipantStatus.DISQUALIFIED
                    ),
                    HamBattleParticipantSpending(
                        "최태양최태양최태양",
                        1_152_600,
                        status = HamBattleParticipantStatus.MISSED_CONSECUTIVE_LOGS
                    )
                ),
                totalCount = 6,
                durationDays = 14,
                startDate = LocalDate.now().minusDays(3)
            ),

            HamBattleChallenge(
                id = "e1",
                type = "1 vs 1",
                title = "이번주 식비 덜 쓰는 사람이 이긴다",
                penalty = "커피 사기",
                participants = listOf(
                    HamBattleParticipantSpending("나", 73_200),
                    HamBattleParticipantSpending("이준혁", 113_000)
                ),
                totalCount = 2,
                durationDays = 7,
                startDate = LocalDate.of(2026, 4, 24)
            ),
            HamBattleChallenge(
                id = "e2",
                type = "그룹",
                title = "5월 식비 절약왕 가리기",
                penalty = "전원에게 삼겹살 쏘기",
                participants = listOf(
                    HamBattleParticipantSpending("김수진", 61_000),
                    HamBattleParticipantSpending("나", 73_200),
                    HamBattleParticipantSpending("박민서", 87_500),
                    HamBattleParticipantSpending(
                        "이준혁",
                        113_000,
                        status = HamBattleParticipantStatus.DISQUALIFIED
                    ),
                    HamBattleParticipantSpending("최태양", 152_600)
                ),
                totalCount = 5,
                durationDays = 14,
                startDate = LocalDate.of(2026, 5, 1)
            )
        )

    fun activeChallenges(referenceToday: LocalDate = LocalDate.now()): List<HamBattleChallenge> =
        seedChallenges().filter { it.status(referenceToday) == HamBattleStatus.ACTIVE }

    fun waitingChallenges(referenceToday: LocalDate = LocalDate.now()): List<HamBattleChallenge> =
        seedChallenges().filter { it.status(referenceToday) == HamBattleStatus.WAITING }

    fun endedChallenges(referenceToday: LocalDate = LocalDate.now()): List<HamBattleChallenge> =
        seedChallenges().filter { it.status(referenceToday) == HamBattleStatus.ENDED }
}

package com.example.hampouch.ui.hambattle

import com.example.hampouch.data.model.HamBattleActiveChallenge
import com.example.hampouch.data.model.HamBattleEndedChallenge
import com.example.hampouch.data.model.HamBattleParticipantSpending
import com.example.hampouch.data.model.HamBattleWaitingChallenge

object HamBattleMockData {

    val activeChallenges = listOf(
        HamBattleActiveChallenge(
            id = "1",
            type = "1 vs 1",
            isOneVsOne = true,
            title = "이번주 식비 덜 쓰는 사람이 이긴다",
            penalty = "커피 사기",
            participants = listOf(
                HamBattleParticipantSpending("나", 73_200),
                HamBattleParticipantSpending("이준혁", 113_000)
            ),
            dDay = "D-3",
            statusMessage = "현재 내가 이기는 중",
            periodLabel = "26.05.01 - 26.05.07 (7일)"
        ),
        HamBattleActiveChallenge(
            id = "2",
            type = "그룹",
            isOneVsOne = false,
            title = "5월 식비 절약왕 가리기",
            penalty = "전원에게 삼겹살 쏘기",
            participants = listOf(
                HamBattleParticipantSpending("김수진", 61_000),
                HamBattleParticipantSpending("나", 73_200),
                HamBattleParticipantSpending("박민서", 87_500),
                HamBattleParticipantSpending("이준혁", 113_000),
                HamBattleParticipantSpending("최태양", 152_600)
            ),
            dDay = "D-11",
            statusMessage = "현재 2위",
            periodLabel = "26.05.01 - 26.05.14 (14일)"
        )
    )

    val endedChallenges = listOf(
        HamBattleEndedChallenge(
            id = "e1",
            type = "1 vs 1",
            isOneVsOne = true,
            title = "이번주 식비 덜 쓰는 사람이 이긴다",
            penalty = "커피 사기",
            participants = listOf(
                HamBattleParticipantSpending("나", 73_200),
                HamBattleParticipantSpending("이준혁", 113_000)
            ),
            periodLabel = "26.04.24 - 26.04.30 (7일)"
        ),
        HamBattleEndedChallenge(
            id = "e2",
            type = "그룹",
            isOneVsOne = false,
            title = "5월 식비 절약왕 가리기",
            penalty = "전원에게 삼겹살 쏘기",
            participants = listOf(
                HamBattleParticipantSpending("김수진", 61_000),
                HamBattleParticipantSpending("나", 73_200),
                HamBattleParticipantSpending("박민서", 87_500),
                HamBattleParticipantSpending("이준혁", 113_000),
                HamBattleParticipantSpending("최태양", 152_600)
            ),
            periodLabel = "26.05.01 - 26.05.14 (14일)"
        )
    )

    val waitingChallenges = listOf(
        HamBattleWaitingChallenge(
            id = "3",
            type = "그룹",
            title = "식비 내기",
            penalty = "간식 사기",
            joinedCount = 1,
            totalCount = 5,
            startsInDay = "D-2",
            startDateLabel = "5월 1일 시작"
        )
    )
}

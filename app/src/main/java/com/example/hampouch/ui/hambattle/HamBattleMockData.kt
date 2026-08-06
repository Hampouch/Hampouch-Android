package com.example.hampouch.ui.hambattle

import androidx.compose.runtime.mutableStateOf
import com.example.hampouch.data.model.HamBattleActiveChallenge
import com.example.hampouch.data.model.HamBattleChallengeRequest
import com.example.hampouch.data.model.HamBattleEndedChallenge
import com.example.hampouch.data.model.HamBattleParticipantSpending
import com.example.hampouch.data.model.HamBattleParticipantStatus
import com.example.hampouch.data.model.HamBattleWaitingChallenge
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// TODO: 서버팀 햄배틀 API 연동 시 목데이터 대신 실제 응답으로 대체.
object HamBattleMockData {

    private val startDateShortFormatter = DateTimeFormatter.ofPattern("MM.dd")
    private val linkTokenChars = ('a'..'z') + ('0'..'9')

    private fun parseParticipantTotalCount(option: String): Int =
        if (option == "1 vs 1") 2 else option.removeSuffix("인").toIntOrNull() ?: 2

    private fun participantTypeLabel(option: String): String =
        if (option == "1 vs 1") "1 vs 1" else "그룹"

    private fun generateRandomBattleLink(): String {
        val token = (1..8).map { linkTokenChars.random() }.joinToString("")
        return "hampouch.app/battle/$token"
    }

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
            periodLabel = "26.05.01 - 26.05.07 (7일)"
        ),
        HamBattleActiveChallenge(
            id = "2",
            type = "그룹",
            isOneVsOne = false,
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
            dDay = "D-11",
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
                HamBattleParticipantSpending(
                    "이준혁",
                    113_000,
                    status = HamBattleParticipantStatus.DISQUALIFIED
                ),
                HamBattleParticipantSpending("최태양", 152_600)
            ),
            periodLabel = "26.05.01 - 26.05.14 (14일)"
        )
    )

    private val waitingChallengesState = mutableStateOf(
        listOf(
            HamBattleWaitingChallenge(
                id = "3",
                type = "그룹",
                title = "식비 내기",
                penalty = "간식 사기",
                participants = listOf(
                    HamBattleParticipantSpending("나", 0)
                ),
                joinedCount = 1,
                totalCount = 10,
                startsInDay = "D-2",
                startDateLabel = "5월 1일 시작",
                startDateShortLabel = "05.01",
                link = "hampouch.app/battle/f3k9j2a1"
            )
        )
    )

    val waitingChallenges: List<HamBattleWaitingChallenge>
        get() = waitingChallengesState.value

    /**
     * 햄배틀 새 챌린지를 대기중인 챌린지 목록에 추가한다.
     */
    fun startNewChallenge(
        request: HamBattleChallengeRequest,
        referenceToday: LocalDate = LocalDate.now()
    ): HamBattleWaitingChallenge {
        val startDate = request.startDateMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
        } ?: referenceToday
        val daysUntilStart = ChronoUnit.DAYS.between(referenceToday, startDate)
        val startsInDay = if (daysUntilStart <= 0) "D-DAY" else "D-$daysUntilStart"

        val newChallenge = HamBattleWaitingChallenge(
            id = "waiting_${System.currentTimeMillis()}",
            type = participantTypeLabel(request.participantCount),
            title = request.challengeName,
            penalty = request.penalty,
            participants = listOf(HamBattleParticipantSpending("나", 0)),
            joinedCount = 1,
            totalCount = parseParticipantTotalCount(request.participantCount),
            startsInDay = startsInDay,
            startDateLabel = "${startDate.monthValue}월 ${startDate.dayOfMonth}일 시작",
            startDateShortLabel = startDate.format(startDateShortFormatter),
            link = generateRandomBattleLink()
        )
        waitingChallengesState.value = waitingChallengesState.value + newChallenge
        return newChallenge
    }

    /**
     * 커뮤니티에서 다른 사람이 모집한 햄배틀 글의 "참가하기"를 누르면 그 챌린지를
     * 나의 대기중인 챌린지 목록에 추가한다. 이미 참가한(같은 링크) 챌린지면 그대로 반환한다.
     */
    fun joinChallengeFromCommunityPost(
        title: String,
        penalty: String,
        link: String,
        totalCount: Int
    ): HamBattleWaitingChallenge {
        waitingChallengesState.value.find { it.link == link }?.let { return it }

        val newChallenge = HamBattleWaitingChallenge(
            id = "waiting_${System.currentTimeMillis()}",
            type = if (totalCount <= 2) "1 vs 1" else "그룹",
            title = title,
            penalty = penalty,
            participants = listOf(HamBattleParticipantSpending("나", 0)),
            joinedCount = 1,
            totalCount = totalCount,
            startsInDay = "D-DAY",
            startDateLabel = "시작일 미정",
            startDateShortLabel = "",
            link = link
        )
        waitingChallengesState.value = waitingChallengesState.value + newChallenge
        return newChallenge
    }
}

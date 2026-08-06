package com.example.hampouch.ui.hambattle

import androidx.compose.runtime.mutableStateOf
import com.example.hampouch.data.model.HamBattleChallenge
import com.example.hampouch.data.model.HamBattleChallengeRequest
import com.example.hampouch.data.model.HamBattleParticipantSpending
import com.example.hampouch.data.model.HamBattleParticipantStatus
import com.example.hampouch.data.model.HamBattleStatus
import com.example.hampouch.ui.expensedetail.ExpenseDetailStore
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

// TODO: 서버팀 햄배틀 API 연동 시 목데이터 대신 실제 응답으로 대체.
object HamBattleMockData {

    private const val DEFAULT_DURATION_DAYS = 7
    private const val ME_NAME = "나"
    private val linkTokenChars = ('a'..'z') + ('0'..'9')

    /** [start]부터 [endInclusive]까지 실제 입력된 지출(ExpenseDetailStore) 합계. */
    private fun mySpentInRange(start: LocalDate, endInclusive: LocalDate): Int {
        if (endInclusive.isBefore(start)) return 0
        var date = start
        var total = 0
        while (!date.isAfter(endInclusive)) {
            total += ExpenseDetailStore.recordsForDate(date).sumOf { it.amount }
            date = date.plusDays(1)
        }
        return total
    }

    /**
     * 진행중인 챌린지의 "나" 참가자 지출 금액을, 실제 입력한 지출 데이터를 합산한 값으로 갈아끼운다.
     * 이미 탈락 처리된 챌린지는 [disqualifyMe]가 확정해둔 금액을 그대로 두고 더 갱신하지 않는다.
     */
    private fun withLiveMySpending(challenge: HamBattleChallenge, referenceToday: LocalDate): HamBattleChallenge {
        if (challenge.status(referenceToday) != HamBattleStatus.ACTIVE) return challenge
        val me = challenge.participants.find { it.name == ME_NAME } ?: return challenge
        if (me.status == HamBattleParticipantStatus.DISQUALIFIED) return challenge
        val start = challenge.effectiveStartDate(referenceToday) ?: return challenge
        val periodEnd = start.plusDays((challenge.durationDays - 1).toLong())
        val soFarEnd = if (referenceToday.isAfter(periodEnd)) periodEnd else referenceToday
        val total = mySpentInRange(start, soFarEnd)
        val updatedParticipants = challenge.participants.map { participant ->
            if (participant.name == ME_NAME) participant.copy(amount = total) else participant
        }
        return challenge.copy(participants = updatedParticipants)
    }

    /** 챌린지 시작일부터 어제까지, "나"의 지출 입력이 며칠 연속으로 비어있는지. */
    fun myMissedStreakDays(challenge: HamBattleChallenge, referenceToday: LocalDate = LocalDate.now()): Int {
        if (challenge.status(referenceToday) != HamBattleStatus.ACTIVE) return 0
        val start = challenge.effectiveStartDate(referenceToday) ?: return 0
        var missed = 0
        var date = referenceToday.minusDays(1)
        while (!date.isBefore(start)) {
            if (ExpenseDetailStore.recordsForDate(date).isNotEmpty()) break
            missed++
            date = date.minusDays(1)
        }
        return missed
    }

    /** 3일 연속 지출 미입력 시 해당 챌린지에서 "나"를 탈락 처리한다 (그 시점까지의 지출 금액으로 고정). */
    fun disqualifyMe(challengeId: String, referenceToday: LocalDate = LocalDate.now()) {
        challengesState.value = challengesState.value.map { challenge ->
            if (challenge.id != challengeId) return@map challenge
            val start = challenge.effectiveStartDate(referenceToday) ?: return@map challenge
            val periodEnd = start.plusDays((challenge.durationDays - 1).toLong())
            val soFarEnd = if (referenceToday.isAfter(periodEnd)) periodEnd else referenceToday
            val finalAmount = mySpentInRange(start, soFarEnd)
            challenge.copy(
                participants = challenge.participants.map { participant ->
                    if (participant.name == ME_NAME) {
                        participant.copy(amount = finalAmount, status = HamBattleParticipantStatus.DISQUALIFIED)
                    } else {
                        participant
                    }
                }
            )
        }
    }

    // "참가가 무효됐어요" 다이얼로그를 이미 확인한 챌린지(계속 다시 뜨지 않도록).
    private val acknowledgedDisqualifications = mutableStateOf(setOf<String>())

    fun isDisqualificationAcknowledged(challengeId: String): Boolean =
        challengeId in acknowledgedDisqualifications.value

    fun acknowledgeDisqualification(challengeId: String) {
        acknowledgedDisqualifications.value = acknowledgedDisqualifications.value + challengeId
    }

    // "이틀 연속 미입력" 경고를 오늘 이미 보여준 챌린지(하루에 한 번만 뜨도록).
    private val missedWarningShownDates = mutableStateOf(mapOf<String, LocalDate>())

    fun wasMissedWarningShownToday(challengeId: String, referenceToday: LocalDate = LocalDate.now()): Boolean =
        missedWarningShownDates.value[challengeId] == referenceToday

    fun markMissedWarningShown(challengeId: String, referenceToday: LocalDate = LocalDate.now()) {
        missedWarningShownDates.value = missedWarningShownDates.value + (challengeId to referenceToday)
    }

    /** 지출 미입력 탈락자가 늘어 남은 참가자가 1명 이하가 되면 챌린지를 강제로 종료시킨다. */
    fun cancelChallenge(challengeId: String) {
        challengesState.value = challengesState.value.map { challenge ->
            if (challenge.id == challengeId) challenge.copy(cancelled = true) else challenge
        }
    }

    // "챌린지가 취소됐어요" 다이얼로그를 이미 확인한 챌린지(계속 다시 뜨지 않도록).
    private val acknowledgedCancellations = mutableStateOf(setOf<String>())

    fun isCancellationAcknowledged(challengeId: String): Boolean =
        challengeId in acknowledgedCancellations.value

    fun acknowledgeCancellation(challengeId: String) {
        acknowledgedCancellations.value = acknowledgedCancellations.value + challengeId
    }

    /** 진행중인 챌린지에서 오늘 하루치만 본 "나" 참가자 지출 금액으로 갈아끼운다. */
    fun participantsForToday(
        challenge: HamBattleChallenge,
        referenceToday: LocalDate = LocalDate.now()
    ): List<HamBattleParticipantSpending> {
        val start = challenge.effectiveStartDate(referenceToday)
        val end = start?.plusDays((challenge.durationDays - 1).toLong())
        val inPeriod = start != null && end != null &&
            !referenceToday.isBefore(start) && !referenceToday.isAfter(end)
        val todayAmount = if (inPeriod) {
            ExpenseDetailStore.recordsForDate(referenceToday).sumOf { it.amount }
        } else {
            0
        }
        return challenge.participants.map { participant ->
            if (participant.name == ME_NAME) participant.copy(amount = todayAmount) else participant
        }
    }

    private fun parseParticipantTotalCount(option: String): Int =
        if (option == "1 vs 1") 2 else option.removeSuffix("인").toIntOrNull() ?: 2

    private fun parseDurationDays(option: String): Int =
        option.removeSuffix("일").toIntOrNull() ?: DEFAULT_DURATION_DAYS

    private fun participantTypeLabel(option: String): String =
        if (option == "1 vs 1") "1 vs 1" else "그룹"

    private fun generateRandomBattleLink(): String {
        val token = (1..8).map { linkTokenChars.random() }.joinToString("")
        return "hampouch.app/battle/$token"
    }

    /**
     * 진행중/대기중/종료 전부 같은 [HamBattleChallenge] 형태를 쓴다. 어느 상태인지는
     * 저장된 값이 아니라 이 리스트를 읽는 쪽(activeChallenges/waitingChallenges/endedChallenges)에서
     * 날짜·인원을 기준으로 매번 계산한다.
     */
    private val challengesState = mutableStateOf(
        listOf(
            // 진행중 — 오늘(참조일) 기준으로 진행중이 되도록 날짜를 잡아둔 목데이터.
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

            // 종료 — 과거에 이미 끝난 날짜.
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
            ),

            // 대기중 — 이미 시작일이 지났는데 인원이 안 차서 자동 취소 대상이 되는 예시.
            HamBattleChallenge(
                id = "3",
                type = "그룹",
                title = "식비 내기",
                penalty = "간식 사기",
                participants = listOf(
                    HamBattleParticipantSpending("나", 0)
                ),
                totalCount = 10,
                durationDays = 7,
                startDate = LocalDate.of(2026, 5, 1),
                link = "hampouch.app/battle/f3k9j2a1"
            )
        )
    )

    val challenges: List<HamBattleChallenge>
        get() {
            val today = LocalDate.now()
            return challengesState.value.map { withLiveMySpending(it, today) }
        }

    fun activeChallenges(referenceToday: LocalDate = LocalDate.now()): List<HamBattleChallenge> =
        challenges.filter { it.status(referenceToday) == HamBattleStatus.ACTIVE }

    fun waitingChallenges(referenceToday: LocalDate = LocalDate.now()): List<HamBattleChallenge> =
        challenges.filter { it.status(referenceToday) == HamBattleStatus.WAITING }

    fun endedChallenges(referenceToday: LocalDate = LocalDate.now()): List<HamBattleChallenge> =
        challenges.filter { it.status(referenceToday) == HamBattleStatus.ENDED }

    /**
     * 햄배틀 새 챌린지를 대기중인 챌린지 목록에 추가한다.
     */
    fun startNewChallenge(
        request: HamBattleChallengeRequest,
        referenceToday: LocalDate = LocalDate.now()
    ): HamBattleChallenge {
        val startDate = request.startDateMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
        } ?: referenceToday

        val newChallenge = HamBattleChallenge(
            id = "waiting_${System.currentTimeMillis()}",
            type = participantTypeLabel(request.participantCount),
            title = request.challengeName,
            penalty = request.penalty,
            participants = listOf(HamBattleParticipantSpending("나", 0)),
            totalCount = parseParticipantTotalCount(request.participantCount),
            durationDays = parseDurationDays(request.durationDays),
            startDate = startDate,
            link = generateRandomBattleLink()
        )
        challengesState.value = challengesState.value + newChallenge
        return newChallenge
    }

    /**
     * 커뮤니티에서 다른 사람이 모집한 햄배틀 글의 "참가하기"를 누르면 그 챌린지를
     * 나의 대기중인 챌린지 목록에 추가한다. 글을 쓴 사람도 참가자로 함께 추가된다.
     * 이미 참가한(같은 링크) 챌린지면 그대로 반환하고, 정원이 이미 다 찼으면 null을 반환한다
     * (호출한 쪽에서 "방이 다 찼습니다" 안내를 띄우면 된다).
     */
    fun joinChallengeFromCommunityPost(
        authorName: String,
        title: String,
        penalty: String,
        link: String,
        totalCount: Int
    ): HamBattleChallenge? {
        val existing = challengesState.value.find { it.link == link }
        if (existing != null) {
            return if (existing.isFull) null else existing
        }

        val newChallenge = HamBattleChallenge(
            id = "waiting_${System.currentTimeMillis()}",
            type = if (totalCount <= 2) "1 vs 1" else "그룹",
            title = title,
            penalty = penalty,
            participants = listOf(
                HamBattleParticipantSpending(authorName, 0),
                HamBattleParticipantSpending("나", 0)
            ),
            totalCount = totalCount,
            durationDays = DEFAULT_DURATION_DAYS,
            startDate = null,
            link = link
        )
        challengesState.value = challengesState.value + newChallenge
        return newChallenge
    }

    /**
     * 시작일까지 참여한 사람이 없어 자동으로 사라진 챌린지 등, 대기중인 챌린지를 목록에서 제거한다.
     */
    fun removeWaitingChallenge(challengeId: String) {
        challengesState.value = challengesState.value.filterNot { it.id == challengeId }
    }
}

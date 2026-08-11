package com.example.hampouch.ui.hambattle

import androidx.compose.runtime.mutableStateOf
import com.example.hampouch.domain.repository.ExpenseRepository
import com.example.hampouch.domain.model.HamBattleChallenge
import com.example.hampouch.domain.model.HamBattleChallengeRequest
import com.example.hampouch.domain.model.HamBattleParticipantSpending
import com.example.hampouch.domain.model.HamBattleParticipantStatus
import com.example.hampouch.domain.model.HamBattleStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

// TODO: 서버팀 햄배틀 API 연동 시 목데이터 대신 실제 응답으로 대체.
object HamBattleMockData {

    /**
     * 햄배틀은 아직 서버 API가 없어 이 목데이터 객체가 상태를 들고 있고, "내 지출"만 실제 기록에서
     * 가져온다. 화면 패키지끼리 직접 얽히지 않도록 지출 조회는 [HampouchApplication]이 Hilt 그래프에서
     * 받아 넣어 준다. 햄배틀이 ViewModel로 옮겨지면 이 객체와 함께 사라진다.
     */
    private var expenseRepository: ExpenseRepository? = null

    fun attach(repository: ExpenseRepository) {
        expenseRepository = repository
    }

    private fun recordsForDate(date: LocalDate) =
        expenseRepository?.recordsForDate(date).orEmpty()

    private fun spentOnDate(date: LocalDate): Int = recordsForDate(date).sumOf { it.amount }

    private const val DEFAULT_DURATION_DAYS = 7
    private const val ME_NAME = "나"
    private val linkTokenChars = ('a'..'z') + ('0'..'9')

    private fun mySpentInRange(start: LocalDate, endInclusive: LocalDate): Int {
        if (endInclusive.isBefore(start)) return 0
        var date = start
        var total = 0
        while (!date.isAfter(endInclusive)) {
            total += spentOnDate(date)
            date = date.plusDays(1)
        }
        return total
    }

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

    fun myMissedStreakDays(challenge: HamBattleChallenge, referenceToday: LocalDate = LocalDate.now()): Int {
        if (challenge.status(referenceToday) != HamBattleStatus.ACTIVE) return 0
        val start = challenge.effectiveStartDate(referenceToday) ?: return 0
        var missed = 0
        var date = referenceToday.minusDays(1)
        while (!date.isBefore(start)) {
            if (recordsForDate(date).isNotEmpty()) break
            missed++
            date = date.minusDays(1)
        }
        return missed
    }

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

    private val acknowledgedDisqualifications = mutableStateOf(setOf<String>())

    fun isDisqualificationAcknowledged(challengeId: String): Boolean =
        challengeId in acknowledgedDisqualifications.value

    fun acknowledgeDisqualification(challengeId: String) {
        acknowledgedDisqualifications.value = acknowledgedDisqualifications.value + challengeId
    }

    private val missedWarningShownDates = mutableStateOf(mapOf<String, LocalDate>())

    fun wasMissedWarningShownToday(challengeId: String, referenceToday: LocalDate = LocalDate.now()): Boolean =
        missedWarningShownDates.value[challengeId] == referenceToday

    fun markMissedWarningShown(challengeId: String, referenceToday: LocalDate = LocalDate.now()) {
        missedWarningShownDates.value = missedWarningShownDates.value + (challengeId to referenceToday)
    }

    fun cancelChallenge(challengeId: String) {
        challengesState.value = challengesState.value.map { challenge ->
            if (challenge.id == challengeId) challenge.copy(cancelled = true) else challenge
        }
    }

    private val acknowledgedCancellations = mutableStateOf(setOf<String>())

    fun isCancellationAcknowledged(challengeId: String): Boolean =
        challengeId in acknowledgedCancellations.value

    fun acknowledgeCancellation(challengeId: String) {
        acknowledgedCancellations.value = acknowledgedCancellations.value + challengeId
    }

    fun participantsForToday(
        challenge: HamBattleChallenge,
        referenceToday: LocalDate = LocalDate.now()
    ): List<HamBattleParticipantSpending> {
        val start = challenge.effectiveStartDate(referenceToday)
        val end = start?.plusDays((challenge.durationDays - 1).toLong())
        val inPeriod = start != null && end != null &&
            !referenceToday.isBefore(start) && !referenceToday.isAfter(end)
        val todayAmount = if (inPeriod) {
            spentOnDate(referenceToday)
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

    private fun buildSeedChallenges(): List<HamBattleChallenge> =
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

    private val challengesState = mutableStateOf(buildSeedChallenges())

    fun resetForAccount() {
        challengesState.value = buildSeedChallenges()
        acknowledgedDisqualifications.value = emptySet()
        missedWarningShownDates.value = emptyMap()
        acknowledgedCancellations.value = emptySet()
    }

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

    fun joinChallengeFromCommunityPost(
        authorName: String,
        title: String,
        penalty: String,
        link: String,
        totalCount: Int,
        referenceToday: LocalDate = LocalDate.now()
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
            startDate = referenceToday,
            link = link
        )
        challengesState.value = challengesState.value + newChallenge
        return newChallenge
    }

    fun removeWaitingChallenge(challengeId: String) {
        challengesState.value = challengesState.value.filterNot { it.id == challengeId }
    }
}

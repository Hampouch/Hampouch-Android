package com.example.hampouch.data.local

import com.example.hampouch.core.config.BattleConfig
import com.example.hampouch.domain.model.HamBattleChallenge
import com.example.hampouch.domain.model.HamBattleChallengeRequest
import com.example.hampouch.domain.model.HamBattleParticipantSpending
import com.example.hampouch.domain.model.HamBattleParticipantStatus
import com.example.hampouch.domain.model.HamBattleStatus
import com.example.hampouch.domain.repository.AccountScopedState
import com.example.hampouch.domain.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

private const val DEFAULT_DURATION_DAYS = 7
private const val ME_NAME = "나"
private val battleCodeChars = ('a'..'z') + ('A'..'Z') + ('0'..'9')

@Singleton
class HamBattleMockStore @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : AccountScopedState {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private fun seedChallenges(): List<HamBattleChallenge> =
        if (BattleConfig.USE_SERVER_BATTLE) emptyList() else HamBattleMockFixtures.seedChallenges()

    private val challengesState = MutableStateFlow(seedChallenges())

    val challenges: StateFlow<List<HamBattleChallenge>> =
        combine(challengesState, expenseRepository.records) { list, _ ->
            val today = LocalDate.now()
            list.map { withLiveMySpending(it, today) }
        }.stateIn(
            scope,
            SharingStarted.Eagerly,
            challengesState.value
        )

    private val acknowledgedDisqualifications = MutableStateFlow(setOf<String>())
    private val missedWarningShownDates = MutableStateFlow(mapOf<String, LocalDate>())
    private val acknowledgedCancellations = MutableStateFlow(setOf<String>())

    private fun recordsForDate(date: LocalDate) = expenseRepository.recordsForDate(date)

    private fun spentOnDate(date: LocalDate): Int = recordsForDate(date).sumOf { it.amount }

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

    private fun withLiveMySpending(
        challenge: HamBattleChallenge,
        referenceToday: LocalDate
    ): HamBattleChallenge {
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

    fun myMissedStreakDays(
        challenge: HamBattleChallenge,
        referenceToday: LocalDate = LocalDate.now()
    ): Int {
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
                        participant.copy(
                            amount = finalAmount,
                            status = HamBattleParticipantStatus.DISQUALIFIED
                        )
                    } else {
                        participant
                    }
                }
            )
        }
    }

    fun isDisqualificationAcknowledged(challengeId: String): Boolean =
        challengeId in acknowledgedDisqualifications.value

    fun acknowledgeDisqualification(challengeId: String) {
        acknowledgedDisqualifications.value = acknowledgedDisqualifications.value + challengeId
    }

    fun wasMissedWarningShownToday(
        challengeId: String,
        referenceToday: LocalDate = LocalDate.now()
    ): Boolean = missedWarningShownDates.value[challengeId] == referenceToday

    fun markMissedWarningShown(
        challengeId: String,
        referenceToday: LocalDate = LocalDate.now()
    ) {
        missedWarningShownDates.value =
            missedWarningShownDates.value + (challengeId to referenceToday)
    }

    fun cancelChallenge(challengeId: String) {
        challengesState.value = challengesState.value.map { challenge ->
            if (challenge.id == challengeId) challenge.copy(cancelled = true) else challenge
        }
    }

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
        val todayAmount = if (inPeriod) spentOnDate(referenceToday) else 0
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

    private fun generateRandomBattleCode(): String =
        (1..7).map { battleCodeChars.random() }.joinToString("")

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
            participants = listOf(HamBattleParticipantSpending(ME_NAME, 0)),
            totalCount = parseParticipantTotalCount(request.participantCount),
            durationDays = parseDurationDays(request.durationDays),
            startDate = startDate,
            battleCode = generateRandomBattleCode()
        )
        challengesState.value = challengesState.value + newChallenge
        return newChallenge
    }

    fun joinChallengeFromCommunityPost(
        authorName: String,
        title: String,
        penalty: String,
        battleCode: String,
        totalCount: Int,
        referenceToday: LocalDate = LocalDate.now()
    ): HamBattleChallenge? {
        val existing = challengesState.value.find { it.battleCode == battleCode }
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
                HamBattleParticipantSpending(ME_NAME, 0)
            ),
            totalCount = totalCount,
            durationDays = DEFAULT_DURATION_DAYS,
            startDate = referenceToday,
            battleCode = battleCode
        )
        challengesState.value = challengesState.value + newChallenge
        return newChallenge
    }

    fun removeWaitingChallenge(challengeId: String) {
        challengesState.value = challengesState.value.filterNot { it.id == challengeId }
    }

    override fun resetForAccount() {
        challengesState.value = seedChallenges()
        acknowledgedDisqualifications.value = emptySet()
        missedWarningShownDates.value = emptyMap()
        acknowledgedCancellations.value = emptySet()
    }
}

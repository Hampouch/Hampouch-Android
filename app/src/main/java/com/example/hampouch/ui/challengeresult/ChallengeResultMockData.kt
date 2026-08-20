package com.example.hampouch.ui.challengeresult

import com.example.hampouch.domain.model.ActiveChallenge
import com.example.hampouch.domain.model.ChallengeResultStatus
import com.example.hampouch.domain.model.ChallengeResultSummary
import com.example.hampouch.ui.challengeresult.ChallengeResultUiState
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.DailyRecordStatus.SUCCESS
import com.example.hampouch.domain.model.EmotionStat
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.SpendingEmotion
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

object ChallengeResultMockData {

    private fun ActiveChallenge.resolvedTotalDays(): Int = totalDays.takeIf { it > 0 }
        ?: (ChronoUnit.DAYS.between(periodStart, periodEnd).toInt() + 1).coerceAtLeast(1)

    private fun reasonIdToEmotion(reasonId: String?): SpendingEmotion = when (reasonId) {
        "stress" -> SpendingEmotion.STRESS
        "reward" -> SpendingEmotion.REWARD
        "lazy" -> SpendingEmotion.LAZY
        "craving" -> SpendingEmotion.CRAVING
        else -> SpendingEmotion.ETC
    }

    private fun computeEmotionStats(records: List<ExpenseRecord>): List<EmotionStat> {
        val totalAmount = records.sumOf { it.amount }
        if (totalAmount <= 0) return SpendingEmotion.entries.map { EmotionStat(it, 0) }
        return SpendingEmotion.entries.map { emotion ->
            val amount = records.filter { reasonIdToEmotion(it.reasonId) == emotion }.sumOf { it.amount }
            EmotionStat(emotion, (amount * 100.0 / totalAmount).roundToInt())
        }
    }

    private fun serverResultOrNull(
        challenge: ActiveChallenge,
        isOngoing: Boolean
    ): ChallengeResultUiState? {
        if (isOngoing) return null
        val summary = challenge.resultSummary ?: return null
        return fromServerResult(challenge, summary)
    }

    fun forChallenge(
        challenge: ActiveChallenge,
        challengeState: ChallengeState,
        recordsForDate: (LocalDate) -> List<ExpenseRecord>,
        hasRecordOnDate: (LocalDate) -> Boolean,
        referenceToday: LocalDate = LocalDate.now()
    ): ChallengeResultUiState {
        val isOngoing = challenge.isOngoingOn(referenceToday)
        val serverResult = serverResultOrNull(challenge, isOngoing)
        val localResult = computeLocalResult(
            challenge,
            challengeState,
            recordsForDate,
            hasRecordOnDate,
            referenceToday
        )
        return serverResult?.copy(
            emotionStats = serverResult.emotionStats.ifEmpty { localResult.emotionStats },
            dailyRecords = serverResult.dailyRecords.ifEmpty { localResult.dailyRecords }
        ) ?: localResult
    }

    private fun computeLocalResult(
        challenge: ActiveChallenge,
        challengeState: ChallengeState,
        recordsForDate: (LocalDate) -> List<ExpenseRecord>,
        hasRecordOnDate: (LocalDate) -> Boolean,
        referenceToday: LocalDate
    ): ChallengeResultUiState {
        val isOngoing = challenge.isOngoingOn(referenceToday)
        val trackedEnd = if (referenceToday.isBefore(challenge.effectivePeriodEnd)) referenceToday else challenge.effectivePeriodEnd
        val recordsInPeriod = generateSequence(challenge.periodStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(trackedEnd) }
            .flatMap { recordsForDate(it) }
            .toList()
        val actualAmount = recordsInPeriod.sumOf { it.amount.toLong() }

        val progress = challengeState.computeProgress(
            referenceToday,
            challenge,
            hasRecordOnDate = hasRecordOnDate
        ) { date -> recordsForDate(date).sumOf { it.amount } }
        val successDays = progress.dailyRecords.values.count { it == SUCCESS }

        val status = when {
            challenge.abandonedDate != null -> ChallengeResultStatus.FAIL
            challenge.remoteStatus == "FAIL" -> ChallengeResultStatus.FAIL
            isOngoing -> ChallengeResultStatus.IN_PROGRESS
            challenge.remoteStatus == "SUCCESS" -> ChallengeResultStatus.COMPLETE
            challenge.remoteStatus != null -> ChallengeResultStatus.FAIL
            actualAmount <= challenge.targetAmount -> ChallengeResultStatus.COMPLETE
            else -> ChallengeResultStatus.FAIL
        }
        val amountLabel = if (status == ChallengeResultStatus.FAIL) "초과 금액" else "총 절약"
        val amountValue = when (status) {
            ChallengeResultStatus.IN_PROGRESS -> progress.savedAmount
            ChallengeResultStatus.FAIL -> (actualAmount - challenge.targetAmount).coerceAtLeast(0)
            ChallengeResultStatus.COMPLETE -> (challenge.targetAmount - actualAmount).coerceAtLeast(0)
        }
        val totalDays = challenge.resolvedTotalDays()

        return ChallengeResultUiState(
            status = status,
            title = "${totalDays}일 챌린지",
            periodStart = challenge.periodStart,
            periodEnd = challenge.periodEnd,
            totalDays = totalDays,
            successDays = successDays,
            streakDays = progress.streakDays,
            amountLabel = amountLabel,
            amountValue = amountValue,
            goalAmount = challenge.targetAmount,
            actualAmount = actualAmount,
            dailyLimit = challenge.dailyLimit,
            emotionStats = computeEmotionStats(recordsInPeriod),
            dailyRecords = progress.dailyRecords,
            isEditable = true
        )
    }

    private fun fromServerResult(
        challenge: ActiveChallenge,
        summary: ChallengeResultSummary
    ): ChallengeResultUiState {
        val status = if (challenge.remoteStatus == "SUCCESS" && challenge.abandonedDate == null) {
            ChallengeResultStatus.COMPLETE
        } else {
            ChallengeResultStatus.FAIL
        }
        val amountLabel = if (status == ChallengeResultStatus.FAIL) "초과 금액" else "총 절약"
        val amountValue = if (status == ChallengeResultStatus.FAIL) summary.overAmount else summary.savedAmount
        val totalDays = challenge.resolvedTotalDays()

        return ChallengeResultUiState(
            status = status,
            title = "${totalDays}일 챌린지",
            periodStart = challenge.periodStart,
            periodEnd = challenge.periodEnd,
            totalDays = totalDays,
            successDays = summary.successDays,
            streakDays = summary.maxStreak,
            amountLabel = amountLabel,
            amountValue = amountValue,
            goalAmount = summary.budgetTotal,
            actualAmount = summary.actualSpent,
            dailyLimit = challenge.dailyLimit,
            emotionStats = challenge.emotionBreakdown,
            dailyRecords = challenge.calendarDays,
            isEditable = true
        )
    }

    fun inProgress(
        challengeState: ChallengeState,
        recordsForDate: (LocalDate) -> List<ExpenseRecord>,
        hasRecordOnDate: (LocalDate) -> Boolean = { true },
        referenceToday: LocalDate = LocalDate.now()
    ): ChallengeResultUiState =
        forChallenge(challengeState.activeChallenge!!, challengeState, recordsForDate, hasRecordOnDate, referenceToday)

    fun recommendedTightenedTarget(actualAmount: Long): Int =
        com.example.hampouch.domain.model.recommendedTightenedTarget(actualAmount)
}

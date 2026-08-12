package com.example.hampouch.ui.challengeresult

import com.example.hampouch.domain.model.ActiveChallenge
import com.example.hampouch.domain.model.ChallengeResultStatus
import com.example.hampouch.ui.challengeresult.ChallengeResultUiState
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.DailyRecordStatus.SUCCESS
import com.example.hampouch.domain.model.EmotionStat
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.SpendingEmotion
import java.time.LocalDate
import kotlin.math.roundToInt

object ChallengeResultMockData {

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

    /** @param recordsForDate 해당 날짜의 지출 내역. */
    fun forChallenge(
        challenge: ActiveChallenge,
        challengeState: ChallengeState,
        recordsForDate: (LocalDate) -> List<ExpenseRecord>,
        referenceToday: LocalDate = LocalDate.now()
    ): ChallengeResultUiState {
        val trackedEnd = if (referenceToday.isBefore(challenge.effectivePeriodEnd)) referenceToday else challenge.effectivePeriodEnd
        val recordsInPeriod = generateSequence(challenge.periodStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(trackedEnd) }
            .flatMap { recordsForDate(it) }
            .toList()
        val actualAmount = recordsInPeriod.sumOf { it.amount }

        val progress = challengeState.computeProgress(referenceToday, challenge) { date ->
            recordsForDate(date).sumOf { it.amount }
        }
        val successDays = progress.dailyRecords.values.count { it == SUCCESS }

        val isActiveChallenge = challenge.id == challengeState.activeChallenge?.id
        val isOngoing = isActiveChallenge && !referenceToday.isAfter(challenge.effectivePeriodEnd)

        val status = when {
            challenge.remoteStatus == "SUCCESS" -> ChallengeResultStatus.COMPLETE
            challenge.remoteStatus == "FAIL" -> ChallengeResultStatus.FAIL
            challenge.abandonedDate != null -> ChallengeResultStatus.FAIL
            isOngoing -> ChallengeResultStatus.IN_PROGRESS
            actualAmount <= challenge.targetAmount -> ChallengeResultStatus.COMPLETE
            else -> ChallengeResultStatus.FAIL
        }
        val amountLabel = if (status == ChallengeResultStatus.FAIL) "초과 금액" else "총 절약"
        val amountValue = when (status) {
            ChallengeResultStatus.IN_PROGRESS -> progress.savedAmount
            ChallengeResultStatus.FAIL -> (actualAmount - challenge.targetAmount).coerceAtLeast(0)
            ChallengeResultStatus.COMPLETE -> (challenge.targetAmount - actualAmount).coerceAtLeast(0)
        }

        return ChallengeResultUiState(
            status = status,
            title = "${challenge.totalDays}일 챌린지",
            periodStart = challenge.periodStart,
            periodEnd = challenge.periodEnd,
            totalDays = challenge.totalDays,
            successDays = successDays,
            streakDays = progress.streakDays,
            amountLabel = amountLabel,
            amountValue = amountValue,
            goalAmount = challenge.targetAmount,
            actualAmount = actualAmount,
            dailyLimit = challenge.dailyLimit,
            emotionStats = computeEmotionStats(recordsInPeriod),
            dailyRecords = progress.dailyRecords,
            isEditable = isActiveChallenge && challengeState.hasOngoingChallenge
        )
    }

    fun inProgress(
        challengeState: ChallengeState,
        recordsForDate: (LocalDate) -> List<ExpenseRecord>,
        referenceToday: LocalDate = LocalDate.now()
    ): ChallengeResultUiState =
        forChallenge(challengeState.activeChallenge!!, challengeState, recordsForDate, referenceToday)

    fun recommendedTightenedTarget(actualAmount: Int): Int =
        com.example.hampouch.domain.model.recommendedTightenedTarget(actualAmount)
}

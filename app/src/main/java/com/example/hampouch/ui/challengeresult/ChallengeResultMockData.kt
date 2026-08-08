package com.example.hampouch.ui.challengeresult

import com.example.hampouch.data.model.ActiveChallenge
import com.example.hampouch.data.model.ChallengeResultStatus
import com.example.hampouch.data.model.ChallengeResultUiState
import com.example.hampouch.data.model.DailyRecordStatus.SUCCESS
import com.example.hampouch.data.model.EmotionStat
import com.example.hampouch.data.model.ExpenseRecord
import com.example.hampouch.data.model.SpendingEmotion
import com.example.hampouch.data.repository.ChallengeRepository
import com.example.hampouch.ui.expensedetail.ExpenseDetailStore
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

    fun forChallenge(challenge: ActiveChallenge, referenceToday: LocalDate = LocalDate.now()): ChallengeResultUiState {
        val trackedEnd = if (referenceToday.isBefore(challenge.effectivePeriodEnd)) referenceToday else challenge.effectivePeriodEnd
        val recordsInPeriod = generateSequence(challenge.periodStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(trackedEnd) }
            .flatMap { ExpenseDetailStore.recordsForDate(it) }
            .toList()
        val actualAmount = recordsInPeriod.sumOf { it.amount }

        val progress = ChallengeRepository.computeProgress(referenceToday, challenge) { date ->
            ExpenseDetailStore.recordsForDate(date).sumOf { it.amount }
        }
        val successDays = progress.dailyRecords.values.count { it == SUCCESS }

        val isActiveChallenge = challenge.id == ChallengeRepository.activeChallenge?.id
        val isOngoing = isActiveChallenge && !referenceToday.isAfter(challenge.effectivePeriodEnd)

        val status = when {
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
            isEditable = isActiveChallenge && ChallengeRepository.hasOngoingChallenge
        )
    }

    fun inProgress(referenceToday: LocalDate = LocalDate.now()): ChallengeResultUiState =
        forChallenge(ChallengeRepository.activeChallenge!!, referenceToday)

    fun recommendedTightenedTarget(actualAmount: Int): Int =
        ((actualAmount / 50_000).coerceAtLeast(1)) * 50_000
}

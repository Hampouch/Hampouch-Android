package com.example.hampouch.domain.model

import java.time.LocalDate

data class ChallengeState(
    val challenges: List<ActiveChallenge> = emptyList(),
    val endAcknowledged: Boolean = false,
    val hasVisitedExpenseEditAfterEnd: Boolean = false,
    val noRecordDates: Set<LocalDate> = emptySet()
) {

    val activeChallenge: ActiveChallenge? get() = challenges.lastOrNull()

    val hasOngoingChallenge: Boolean
        get() = activeChallenge?.let { !LocalDate.now().isAfter(it.effectivePeriodEnd) } ?: false

    fun isChallengeJustEnded(referenceToday: LocalDate): Boolean =
        activeChallenge?.let { !endAcknowledged && referenceToday.isAfter(it.periodEnd) } ?: false

    fun challengeFor(date: LocalDate): ActiveChallenge? =
        challenges.firstOrNull { !date.isBefore(it.periodStart) && !date.isAfter(it.effectivePeriodEnd) }

    fun challengeById(id: String): ActiveChallenge? = challenges.find { it.id == id }

    fun elapsedDays(referenceToday: LocalDate, challenge: ActiveChallenge): List<LocalDate> {
        val trackedEnd =
            if (referenceToday.isBefore(challenge.effectivePeriodEnd)) referenceToday else challenge.effectivePeriodEnd
        return generateSequence(challenge.periodStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(trackedEnd) }
            .toList()
    }

    fun computeProgress(
        referenceToday: LocalDate,
        challenge: ActiveChallenge,
        spentOnDate: (LocalDate) -> Int
    ): ChallengeProgress {
        val days = elapsedDays(referenceToday, challenge)
        fun balanceOn(date: LocalDate) = challenge.dailyLimitOn(date) - spentOnDate(date)
        fun isSuccess(date: LocalDate) = date !in noRecordDates && balanceOn(date) >= 0

        val savedAmount = days.sumOf { balanceOn(it) }
        var streakDays = 0
        for (day in days.asReversed()) {
            if (isSuccess(day)) streakDays++ else break
        }
        val dailyRecords = days.associateWith { day ->
            if (isSuccess(day)) DailyRecordStatus.SUCCESS else DailyRecordStatus.FAIL
        }
        return ChallengeProgress(savedAmount = savedAmount, streakDays = streakDays, dailyRecords = dailyRecords)
    }
}

fun recommendedTightenedTarget(actualAmount: Int): Int =
    ((actualAmount / 50_000).coerceAtLeast(1)) * 50_000

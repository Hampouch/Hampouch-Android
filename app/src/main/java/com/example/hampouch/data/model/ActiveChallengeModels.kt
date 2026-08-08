package com.example.hampouch.data.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class DailyLimitOverride(
    val effectiveFrom: LocalDate,
    val dailyLimit: Int
)

data class ActiveChallenge(
    val id: String,
    val totalDays: Int,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val dailyLimit: Int,
    val targetAmount: Int,
    val savedAmount: Int,
    val streakDays: Int,
    val editCount: Int,
    val repeatMonthly: Boolean = false,
    val dailyLimitOverrides: List<DailyLimitOverride> = listOf(DailyLimitOverride(periodStart, dailyLimit)),
    val abandonedDate: LocalDate? = null
) {
    val maxEditCount: Int
        get() = if (totalDays >= 15) 2 else 1

    val canEditTarget: Boolean
        get() = editCount < maxEditCount

    val effectivePeriodEnd: LocalDate
        get() = abandonedDate?.minusDays(1) ?: periodEnd

    fun dDayFrom(referenceToday: LocalDate): Int =
        ChronoUnit.DAYS.between(referenceToday, periodEnd).toInt()

    fun dailyLimitOn(date: LocalDate): Int {
        var result = dailyLimit
        var bestEffectiveFrom: LocalDate? = null
        for (override in dailyLimitOverrides) {
            if (!override.effectiveFrom.isAfter(date) &&
                (bestEffectiveFrom == null || !override.effectiveFrom.isBefore(bestEffectiveFrom))
            ) {
                bestEffectiveFrom = override.effectiveFrom
                result = override.dailyLimit
            }
        }
        return result
    }
}

data class ChallengeProgress(
    val savedAmount: Int,
    val streakDays: Int,
    val dailyRecords: Map<LocalDate, DailyRecordStatus>
)

package com.example.hampouch.ui.onboarding

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlin.math.roundToLong

internal const val FIXED_DATE_DAILY_TARGET_DIVISOR = 30

object OnboardingCalculations {

    /**
     * The daily food-spending goal. For "날짜 고정" (fixed date) mode this is always the total
     * goal divided by 30, regardless of the actual gap-period day count, since the total goal
     * represents a full monthly cycle. For period mode it's the total divided by the chosen
     * period length.
     */
    fun dailyTarget(state: OnboardingUiState, effectiveTotal: Int, periodDays: Int): Int = if (state.dateFixed) {
        (effectiveTotal.toDouble() / FIXED_DATE_DAILY_TARGET_DIVISOR).roundToInt()
    } else {
        (effectiveTotal.toDouble() / periodDays).roundToInt()
    }

    fun impliedPeriodDays(state: OnboardingUiState, referenceToday: LocalDate = LocalDate.now()): Int? = when {
        state.periodEnabled -> state.challengePeriodDays
        state.dateFixed -> {
            val anchor = state.startDate
            val periodEnd = if (anchor != null && anchor.isAfter(referenceToday)) {
                anchor.minusDays(1)
            } else {
                referenceToday.plusMonths(1).minusDays(1)
            }
            ChronoUnit.DAYS.between(referenceToday, periodEnd).toInt() + 1
        }
        else -> null
    }

    fun recommendedTotalTarget(lastMonthFoodExpense: Int?, periodDays: Int?): Int? {
        if (lastMonthFoodExpense == null || periodDays == null || periodDays <= 0) return null
        val rawRecommendation = lastMonthFoodExpense.toDouble() / 30 * periodDays * 0.9
        val roundedToThousands = (rawRecommendation / 1000.0).roundToLong() * 1000
        return roundedToThousands.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
    }
}

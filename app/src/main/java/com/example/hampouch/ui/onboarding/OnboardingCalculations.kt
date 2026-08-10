package com.example.hampouch.ui.onboarding

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

object OnboardingCalculations {

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
        return (rawRecommendation / 1000.0).roundToInt() * 1000
    }
}

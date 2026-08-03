package com.example.hampouch.ui.onboarding

import kotlin.math.roundToInt

object OnboardingCalculations {

    fun impliedPeriodDays(state: OnboardingUiState): Int? = when {
        state.periodEnabled -> state.challengePeriodDays
        state.dateFixed -> 30
        else -> null
    }

    fun recommendedTotalTarget(lastMonthFoodExpense: Int?, periodDays: Int?): Int? {
        if (lastMonthFoodExpense == null || periodDays == null || periodDays <= 0) return null
        val rawRecommendation = lastMonthFoodExpense.toDouble() / 30 * periodDays * 0.9
        return (rawRecommendation / 1000.0).roundToInt() * 1000
    }
}

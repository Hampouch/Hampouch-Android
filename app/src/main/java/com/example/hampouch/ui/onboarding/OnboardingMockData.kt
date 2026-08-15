package com.example.hampouch.ui.onboarding

import com.example.hampouch.R

data class PeriodPreset(
    val days: Int,
    val labelResId: Int
)

object OnboardingMockData {
    val periodPresets = listOf(
        PeriodPreset(days = 7, labelResId = R.string.onboarding_period_7days),
        PeriodPreset(days = 14, labelResId = R.string.onboarding_period_14days),
        PeriodPreset(days = 30, labelResId = R.string.onboarding_period_30days)
    )
}

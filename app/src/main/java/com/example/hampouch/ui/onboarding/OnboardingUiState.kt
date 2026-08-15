package com.example.hampouch.ui.onboarding

import java.time.LocalDate

data class OnboardingUiState(
    val lastMonthFoodExpense: Int? = null,
    val periodEnabled: Boolean = false,
    val challengePeriodDays: Int? = null,
    val dateFixed: Boolean = false,
    val startDate: LocalDate? = null,
    val totalTargetAmount: Int? = null
)

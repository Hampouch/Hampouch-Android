package com.example.hampouch.ui.onboarding

import java.time.LocalDate

data class OnboardingUiState(
    val lastMonthFoodExpense: Int? = null,
    val challengePeriodDays: Int? = null,
    val dateFixed: Boolean = true,
    val startDate: LocalDate? = null,
    val dailyTargetAmount: Int? = null,
    val selectedCategoryIds: Set<String> = emptySet()
)

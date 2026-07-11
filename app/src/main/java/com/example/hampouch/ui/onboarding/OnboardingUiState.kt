package com.example.hampouch.ui.onboarding

data class OnboardingUiState(
    val lastMonthFoodExpense: Int? = null,
    val challengePeriodDays: Int = OnboardingMockData.periodPresets.last().days,
    val salaryDay: Int? = null,
    val targetAmount: Int? = null,
    val selectedCategoryIds: Set<String> = emptySet()
)

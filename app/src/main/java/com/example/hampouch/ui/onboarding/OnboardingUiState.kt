package com.example.hampouch.ui.onboarding

data class OnboardingUiState(
    val lastMonthFoodExpense: Int? = null,
    val challengePeriodDays: Int = 7,
    val salaryDay: Int? = null,
    val resetOnSalaryDay: Boolean = true,
    val targetAmount: Int? = null,
    val selectedCategoryIds: Set<String> = emptySet()
)

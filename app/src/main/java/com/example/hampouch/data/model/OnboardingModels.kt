package com.example.hampouch.data.model

enum class ChallengePeriodType {
    ONE_WEEK,
    TWO_WEEKS,
    ONE_MONTH,
    CUSTOM
}

data class SpendingCategory(
    val id: String,
    val labelResId: Int,
    val iconResId: Int? = null
)

data class OnboardingRequest(
    val lastMonthFoodExpense: Int? = null,
    val challengePeriodType: ChallengePeriodType = ChallengePeriodType.ONE_MONTH,
    val customPeriodDays: Int? = null,
    val salaryDay: Int? = null,
    val targetAmount: Int? = null,
    val topSpendingCategoryIds: List<String> = emptyList()
)

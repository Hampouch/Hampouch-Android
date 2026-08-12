package com.example.hampouch.domain.model

import java.time.LocalDate

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
    val dateFixed: Boolean = true,
    val startDate: LocalDate? = null,
    val dailyTargetAmount: Int? = null,
    val totalTargetAmount: Int? = null,
    val topSpendingCategoryIds: List<String> = emptyList()
)

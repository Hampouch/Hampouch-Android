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

sealed interface ChallengePeriod {
    data class FixedStart(val startDate: LocalDate) : ChallengePeriod

    data class Duration(val days: Int) : ChallengePeriod {
        init {
            require(days > 0) { "챌린지 기간은 1일 이상이어야 합니다." }
        }
    }
}

data class OnboardingRequest(
    val lastMonthFoodExpense: Int? = null,
    val period: ChallengePeriod,
    val dailyTargetAmount: Int,
    val totalTargetAmount: Int,
    val topSpendingCategoryIds: List<String> = emptyList()
) {
    val dateFixed: Boolean get() = period is ChallengePeriod.FixedStart
    val startDate: LocalDate? get() = (period as? ChallengePeriod.FixedStart)?.startDate
    val customPeriodDays: Int? get() = (period as? ChallengePeriod.Duration)?.days
    val challengePeriodType: ChallengePeriodType
        get() = when (customPeriodDays) {
            7 -> ChallengePeriodType.ONE_WEEK
            14 -> ChallengePeriodType.TWO_WEEKS
            30 -> ChallengePeriodType.ONE_MONTH
            else -> ChallengePeriodType.CUSTOM
        }
}

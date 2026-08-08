package com.example.hampouch.ui.onboarding

import androidx.compose.runtime.saveable.Saver
import java.time.LocalDate

data class OnboardingUiState(
    val lastMonthFoodExpense: Int? = null,
    val periodEnabled: Boolean = false,
    val challengePeriodDays: Int? = null,
    val dateFixed: Boolean = false,
    val startDate: LocalDate? = null,
    val totalTargetAmount: Int? = null,
    val selectedCategoryIds: Set<String> = emptySet()
)

val OnboardingUiStateSaver = Saver<OnboardingUiState, List<Any?>>(
    save = {
        listOf(
            it.lastMonthFoodExpense,
            it.periodEnabled,
            it.challengePeriodDays,
            it.dateFixed,
            it.startDate?.toEpochDay(),
            it.totalTargetAmount,
            it.selectedCategoryIds.toList()
        )
    },
    restore = { saved ->
        @Suppress("UNCHECKED_CAST")
        OnboardingUiState(
            lastMonthFoodExpense = saved[0] as Int?,
            periodEnabled = saved[1] as Boolean,
            challengePeriodDays = saved[2] as Int?,
            dateFixed = saved[3] as Boolean,
            startDate = (saved[4] as Long?)?.let(LocalDate::ofEpochDay),
            totalTargetAmount = saved[5] as Int?,
            selectedCategoryIds = (saved[6] as List<String>).toSet()
        )
    }
)

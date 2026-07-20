package com.example.hampouch.ui.home

import java.time.LocalDate

enum class CharacterState {
    CHUBBY,
    NORMAL,
    THIN;

    companion object {
        fun fromRatio(ratio: Float): CharacterState = when {
            ratio >= 0.7f -> CHUBBY
            ratio >= 0.3f -> NORMAL
            else -> THIN
        }
    }
}

data class HomeChallenge(
    val totalDays: Int,
    val dDay: Int,
    val periodStartLabel: String,
    val periodEndLabel: String,
    val dailyLimit: Int,
    val todayBalance: Int,
    val savedAmount: Int,
    val streakDays: Int
) {
    val balanceRatio: Float
        get() = if (dailyLimit <= 0) 0f else (todayBalance.toFloat() / dailyLimit.toFloat()).coerceIn(0f, 1f)

    val characterState: CharacterState
        get() = CharacterState.fromRatio(balanceRatio)
}

data class ExpenseEntry(
    val id: String,
    val categoryId: String? = null,
    val customCategoryName: String? = null,
    val name: String? = null,
    val reasonTag: String? = null,
    val amount: Int
)

data class MiniChallengeEntry(
    val id: String,
    val name: String,
    val periodLabel: String,
    val isChecked: Boolean
)

enum class WarningVariant {
    SUGGESTION,
    ALERT
}

data class HomeWarning(
    val id: String,
    val variant: WarningVariant,
    val title: String,
    val message: String
)

data class HomeUiState(
    val userName: String,
    val selectedDate: LocalDate,
    val challenge: HomeChallenge?,
    val expenses: List<ExpenseEntry> = emptyList(),
    val miniChallenges: List<MiniChallengeEntry> = emptyList(),
    val warnings: List<HomeWarning> = emptyList()
)

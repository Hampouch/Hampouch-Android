package com.example.hampouch.domain.model

import java.time.LocalDate

enum class CharacterState {
    CHUBBY,
    NORMAL,
    THIN,
    OVER_LIMIT;

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
    val streakDays: Int,
    val isEnded: Boolean = false
) {
    val isOverLimit: Boolean
        get() = todayBalance < 0

    val balanceRatio: Float
        get() = when {
            isOverLimit -> 1f
            dailyLimit <= 0 -> 0f
            else -> (todayBalance.toFloat() / dailyLimit.toFloat()).coerceIn(0f, 1f)
        }

    val characterState: CharacterState
        get() = if (isOverLimit) CharacterState.OVER_LIMIT else CharacterState.fromRatio(balanceRatio)
}

data class ExpenseEntry(
    val id: String,
    val categoryId: String? = null,
    val customCategoryName: String? = null,
    val name: String? = null,
    val reasonTag: String? = null,
    val amount: Int
)

enum class HomeWarningVariant {
    ALERT,
    REMINDER
}

/**
 * [serverCode]는 "진행 중 챌린지 + 현황"(`GET /api/challenges/current`) 응답의
 * `warningCards[].type` 값과 매칭된다. 명세상 현재 warningCards는 항상 빈 배열이며,
 * 확정된 코드는 CATEGORY_OVERSPEND의 WEAK_CATEGORY_ALERT 하나뿐이다(#52에서 서버 구현 예정).
 * 나머지 코드는 백엔드 확정 전 잠정값이므로, 실제 코드가 정해지면 이 값만 갱신하면 된다.
 */
enum class HomeWarningType(val variant: HomeWarningVariant, val serverCode: String) {
    LOW_DAILY_BUDGET(HomeWarningVariant.ALERT, "LOW_DAILY_BUDGET"),
    CHALLENGE_BUDGET_EXCEEDED(HomeWarningVariant.ALERT, "CHALLENGE_BUDGET_EXCEEDED"),
    CATEGORY_OVERSPEND(HomeWarningVariant.ALERT, "WEAK_CATEGORY_ALERT"),
    REASON_OVERSPEND(HomeWarningVariant.ALERT, "REASON_OVERSPEND"),
    LARGE_SINGLE_EXPENSE(HomeWarningVariant.ALERT, "LARGE_SINGLE_EXPENSE"),
    MISSED_YESTERDAY_RECORD(HomeWarningVariant.REMINDER, "MISSED_YESTERDAY_RECORD");

    companion object {
        fun fromServerCode(code: String): HomeWarningType? = entries.find { it.serverCode == code }
    }
}

data class HomeWarning(
    val id: String,
    val type: HomeWarningType,
    val categoryId: String? = null,
    val categorySpentAmount: Int? = null,
    val reasonLabel: String? = null
)

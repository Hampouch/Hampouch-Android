package com.example.hampouch.domain.model

import java.time.LocalDate

enum class ChallengeResultStatus {
    COMPLETE,
    FAIL,
    IN_PROGRESS
}

enum class DailyRecordStatus {
    SUCCESS,
    FAIL
}

enum class SpendingEmotion {
    CRAVING,
    STRESS,
    LAZY,
    REWARD,
    ETC
}

data class EmotionStat(
    val emotion: SpendingEmotion,
    val percent: Int,
    val amount: Long = 0
)

data class ChallengeResultSummary(
    val successDays: Int,
    val overDays: Int,
    val savedAmount: Long,
    val overAmount: Long,
    val maxStreak: Int,
    val budgetTotal: Int,
    val actualSpent: Long
)

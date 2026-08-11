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
    val percent: Int
)

data class ChallengeResultUiState(
    val status: ChallengeResultStatus,
    val title: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val totalDays: Int,
    val successDays: Int,
    val streakDays: Int,
    val amountLabel: String,
    val amountValue: Int,
    val goalAmount: Int,
    val actualAmount: Int,
    val dailyLimit: Int,
    val emotionStats: List<EmotionStat>,
    val dailyRecords: Map<LocalDate, DailyRecordStatus>,
    val isEditable: Boolean = false
)

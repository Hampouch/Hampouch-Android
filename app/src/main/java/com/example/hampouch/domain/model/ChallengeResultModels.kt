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

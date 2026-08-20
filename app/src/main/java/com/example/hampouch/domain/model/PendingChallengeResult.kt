package com.example.hampouch.domain.model

import java.time.LocalDate

data class PendingChallengeResult(
    val userId: Long,
    val challengeId: String,
    val title: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val totalDays: Int,
    val successDays: Int,
    val streakDays: Int,
    val amountValue: Long,
    val goalAmount: Int,
    val actualAmount: Long,
    val dailyLimit: Int,
    val emotionStats: List<EmotionStat>,
    val dailyRecords: Map<LocalDate, DailyRecordStatus>
)

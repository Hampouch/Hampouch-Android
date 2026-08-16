package com.example.hampouch.ui.challengeresult

import com.example.hampouch.domain.model.ChallengeResultStatus
import com.example.hampouch.domain.model.DailyRecordStatus
import com.example.hampouch.domain.model.EmotionStat
import java.time.LocalDate

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

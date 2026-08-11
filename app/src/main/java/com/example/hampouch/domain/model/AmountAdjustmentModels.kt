package com.example.hampouch.domain.model

import java.time.LocalDate

data class AmountAdjustmentChallenge(
    val id: String,
    val totalDays: Int,
    val dDay: Int,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val targetAmount: Int,
    val dailyLimit: Int,
    val overAmount: Int,
    val editCount: Int
) {
    val maxEditCount: Int
        get() = if (totalDays >= 15) 2 else 1

    val canEdit: Boolean
        get() = editCount < maxEditCount
}

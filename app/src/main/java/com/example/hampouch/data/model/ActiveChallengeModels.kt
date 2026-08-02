package com.example.hampouch.data.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class ActiveChallenge(
    val id: String,
    val totalDays: Int,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val dailyLimit: Int,
    val targetAmount: Int,
    val savedAmount: Int,
    val streakDays: Int,
    val editCount: Int
) {
    val maxEditCount: Int
        get() = if (totalDays >= 15) 2 else 1

    val canEditTarget: Boolean
        get() = editCount < maxEditCount

    fun dDayFrom(referenceToday: LocalDate): Int =
        ChronoUnit.DAYS.between(referenceToday, periodEnd).toInt()
}

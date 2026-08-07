package com.example.hampouch.data.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class MiniChallengeEntry(
    val id: String,
    val name: String,
    val totalDays: Int? = null,
    val achievedDays: Int = 0,
    val isChecked: Boolean = false,
    val startDate: LocalDate = LocalDate.now()
) {
    fun periodLabel(referenceToday: LocalDate = LocalDate.now()): String {
        if (totalDays == null) return "오늘만"
        val daysElapsed = ChronoUnit.DAYS.between(startDate, referenceToday).toInt()
        val daysRemaining = (totalDays - 1 - daysElapsed).coerceAtLeast(0)
        return if (daysRemaining <= 0) "D-DAY" else "D-$daysRemaining"
    }
}

data class RecommendedMiniChallenge(
    val id: String,
    val totalDays: Int?,
    val name: String
) {
    val periodLabel: String
        get() = if (totalDays == null) "오늘만" else "${totalDays}일간"
}

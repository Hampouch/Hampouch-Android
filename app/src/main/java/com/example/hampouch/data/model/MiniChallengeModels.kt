package com.example.hampouch.data.model

data class MiniChallengeEntry(
    val id: String,
    val name: String,
    val totalDays: Int? = null,
    val achievedDays: Int = 0,
    val isChecked: Boolean = false
) {
    val periodLabel: String
        get() = when {
            totalDays == null -> "오늘만"
            achievedDays <= 0 -> "${totalDays}일간"
            else -> "${achievedDays.coerceAtMost(totalDays)}/${totalDays}일"
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

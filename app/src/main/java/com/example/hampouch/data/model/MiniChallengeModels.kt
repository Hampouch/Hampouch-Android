package com.example.hampouch.data.model

import java.time.LocalDate

data class MiniChallengeEntry(
    val id: String,
    val name: String,
    val totalDays: Int? = null,
    /** 서버(GET /api/mini-challenges)의 progressDays — 조회 date 기준으로 서버가 매번 다시 계산해 주는
     * "며칠째"값. 여기서 남은 일수를 역산하면 클라이언트가 startDate를 따로 들고 있을 필요가 없다. */
    val achievedDays: Int = 0,
    val isChecked: Boolean = false,
    val startDate: LocalDate = LocalDate.now()
) {
    fun periodLabel(): String {
        if (totalDays == null) return "오늘만"
        val daysRemaining = (totalDays - achievedDays).coerceAtLeast(0)
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

/**
 * 서버(/api/mini-challenges)가 조회 시점(as-of date)마다 다시 계산해 내려주는 그날의 집계값.
 * 클라이언트가 개별 항목으로부터 근사 계산하던 값(예: streakDays)을 서버 값으로 대체하는 데 쓴다.
 */
data class MiniChallengeDaySummary(
    val checkedCount: Int,
    val totalCount: Int,
    val streakDays: Int
)

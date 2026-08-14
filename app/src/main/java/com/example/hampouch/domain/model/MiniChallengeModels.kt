package com.example.hampouch.domain.model

import java.time.LocalDate

sealed interface MiniChallengeDuration {
    val serverDays: Int

    data object Today : MiniChallengeDuration {
        override val serverDays: Int = 1
    }

    data class Period(override val serverDays: Int) : MiniChallengeDuration {
        init {
            require(serverDays > 1) { "기간 지정 미니 챌린지는 2일 이상이어야 합니다." }
        }
    }
}

fun miniChallengeDuration(days: Int): MiniChallengeDuration =
    if (days == 1) MiniChallengeDuration.Today else MiniChallengeDuration.Period(days)

data class MiniChallengeEntry(
    val id: String,
    val name: String,
    val duration: MiniChallengeDuration,
    val achievedDays: Int = 0,
    val isChecked: Boolean = false,
    val startDate: LocalDate = LocalDate.now()
) {
    fun periodLabel(): String {
        if (duration is MiniChallengeDuration.Today) return "오늘만"
        val daysRemaining = (duration.serverDays - achievedDays).coerceAtLeast(0)
        return if (daysRemaining <= 0) "D-DAY" else "D-$daysRemaining"
    }
}

data class RecommendedMiniChallenge(
    val id: String,
    val duration: MiniChallengeDuration,
    val name: String
) {
    val periodLabel: String
        get() = if (duration is MiniChallengeDuration.Today) "오늘만" else "${duration.serverDays}일간"
}

data class MiniChallengeDaySummary(
    val checkedCount: Int,
    val totalCount: Int,
    val streakDays: Int
)

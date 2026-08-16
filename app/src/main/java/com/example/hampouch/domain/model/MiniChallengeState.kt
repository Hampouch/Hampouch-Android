package com.example.hampouch.domain.model

import java.time.LocalDate

data class MiniChallengeState(
    val challengesByDate: Map<LocalDate, List<MiniChallengeEntry>> = emptyMap(),
    val recommendedChallenges: List<RecommendedMiniChallenge> = emptyList(),
    val summaryByDate: Map<LocalDate, MiniChallengeDaySummary> = emptyMap()
) {
    fun challengesFor(date: LocalDate): List<MiniChallengeEntry> = challengesByDate[date].orEmpty()

    fun summaryFor(date: LocalDate): MiniChallengeDaySummary? = summaryByDate[date]

    fun isNameTaken(date: LocalDate, name: String): Boolean {
        val normalized = normalizeMiniChallengeName(name)
        return challengesFor(date).any { normalizeMiniChallengeName(it.name) == normalized }
    }
}

fun normalizeMiniChallengeName(name: String): String =
    name.replace(Regex("\\s+"), "").lowercase()

package com.example.hampouch.ui.minichallenge

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.hampouch.data.model.MiniChallengeEntry
import com.example.hampouch.data.model.RecommendedMiniChallenge
import java.time.LocalDate
import java.util.UUID

object MiniChallengeStore {

    var challengesByDate: Map<LocalDate, List<MiniChallengeEntry>> by mutableStateOf(
        mapOf(
            LocalDate.now().minusDays(1) to MiniChallengeMockData.yesterdayChallenges(),
            LocalDate.now() to MiniChallengeMockData.todayChallenges()
        )
    )
        private set

    var recommendedChallenges: List<RecommendedMiniChallenge> by mutableStateOf(MiniChallengeMockData.recommendedChallenges())
        private set

    fun challengesFor(date: LocalDate): List<MiniChallengeEntry> = challengesByDate[date].orEmpty()

    /** Normalizes a challenge name for duplicate comparison: ignores surrounding/inner whitespace and case. */
    fun normalizeName(name: String): String = name.replace(Regex("\\s+"), "").lowercase()

    fun isNameTaken(date: LocalDate, name: String): Boolean {
        val normalized = normalizeName(name)
        return challengesFor(date).any { normalizeName(it.name) == normalized }
    }

    fun toggle(date: LocalDate, id: String) {
        val updated = challengesFor(date).map { entry ->
            if (entry.id != id) return@map entry
            entry.copy(isChecked = !entry.isChecked)
        }
        challengesByDate = challengesByDate + (date to updated)
    }

    /** Adds a mini challenge for [date]. Returns false without adding if the name already exists for that date. */
    fun addChallenge(date: LocalDate, name: String, totalDays: Int?): Boolean {
        val trimmedName = name.trim().ifBlank { "이름 없는 챌린지" }
        if (isNameTaken(date, trimmedName)) return false
        val entry = MiniChallengeEntry(
            id = UUID.randomUUID().toString(),
            name = trimmedName,
            totalDays = totalDays,
            achievedDays = 0,
            isChecked = false,
            startDate = date
        )
        challengesByDate = challengesByDate + (date to (challengesFor(date) + entry))
        return true
    }

    /** Adds [recommended] as a mini challenge for [date]. Returns false without adding if the name already exists for that date. */
    fun addRecommendedChallenge(date: LocalDate, recommended: RecommendedMiniChallenge): Boolean {
        val added = addChallenge(date, recommended.name, recommended.totalDays)
        if (added) {
            recommendedChallenges = recommendedChallenges.filterNot { it.id == recommended.id }
        }
        return added
    }

    fun removeChallenge(date: LocalDate, id: String) {
        challengesByDate = challengesByDate + (date to challengesFor(date).filterNot { it.id == id })
    }

    fun resetForAccount() {
        challengesByDate = mapOf(
            LocalDate.now().minusDays(1) to MiniChallengeMockData.yesterdayChallenges(),
            LocalDate.now() to MiniChallengeMockData.todayChallenges()
        )
        recommendedChallenges = MiniChallengeMockData.recommendedChallenges()
    }
}

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

    fun toggle(date: LocalDate, id: String) {
        val updated = challengesFor(date).map { entry ->
            if (entry.id != id) return@map entry
            val nowChecked = !entry.isChecked
            val totalDays = entry.totalDays
            val newAchievedDays = if (totalDays == null) {
                entry.achievedDays
            } else {
                (entry.achievedDays + if (nowChecked) 1 else -1).coerceIn(0, totalDays)
            }
            entry.copy(isChecked = nowChecked, achievedDays = newAchievedDays)
        }
        challengesByDate = challengesByDate + (date to updated)
    }

    fun addChallenge(date: LocalDate, name: String, totalDays: Int?) {
        val entry = MiniChallengeEntry(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "이름 없는 챌린지" },
            totalDays = totalDays,
            achievedDays = 0,
            isChecked = false
        )
        challengesByDate = challengesByDate + (date to (challengesFor(date) + entry))
    }

    fun addRecommendedChallenge(date: LocalDate, recommended: RecommendedMiniChallenge) {
        addChallenge(date, recommended.name, recommended.totalDays)
        recommendedChallenges = recommendedChallenges.filterNot { it.id == recommended.id }
    }
}

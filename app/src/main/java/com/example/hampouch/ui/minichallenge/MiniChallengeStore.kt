package com.example.hampouch.ui.minichallenge

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.hampouch.data.model.MiniChallengeDaySummary
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

    /** 서버 모드에서만 채워진다. 목데이터 모드에서는 비어 있고, 화면단이 개별 항목으로부터 근사 계산한다. */
    var summaryByDate: Map<LocalDate, MiniChallengeDaySummary> by mutableStateOf(emptyMap())
        private set

    fun challengesFor(date: LocalDate): List<MiniChallengeEntry> = challengesByDate[date].orEmpty()

    fun summaryFor(date: LocalDate): MiniChallengeDaySummary? = summaryByDate[date]

    /** 서버(/api/mini-challenges) 조회 결과의 summary를 [date]에 반영한다. */
    fun setSummaryForDate(date: LocalDate, summary: MiniChallengeDaySummary) {
        summaryByDate = summaryByDate + (date to summary)
    }

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

    /**
     * 서버(/api/mini-challenges) 조회 결과로 [date]의 목록을 통째로 교체한다.
     * 목데이터 모드의 [addChallenge]/[toggle] 등과 달리 서버가 이미 계산해 준 값을 그대로 반영할 때 쓴다.
     */
    fun setChallengesForDate(date: LocalDate, entries: List<MiniChallengeEntry>) {
        challengesByDate = challengesByDate + (date to entries)
    }

    /** 서버(/api/mini-challenges/recommended) 조회 결과로 추천 목록을 통째로 교체한다. */
    fun replaceRecommendedChallenges(items: List<RecommendedMiniChallenge>) {
        recommendedChallenges = items
    }

    /** 추천 카탈로그에서 [id]를 방금 추가했으니 추천 목록에서 제거한다(목데이터 [addRecommendedChallenge]와 동일한 효과). */
    fun removeRecommended(id: String) {
        recommendedChallenges = recommendedChallenges.filterNot { it.id == id }
    }

    fun resetForAccount() {
        challengesByDate = mapOf(
            LocalDate.now().minusDays(1) to MiniChallengeMockData.yesterdayChallenges(),
            LocalDate.now() to MiniChallengeMockData.todayChallenges()
        )
        recommendedChallenges = MiniChallengeMockData.recommendedChallenges()
        summaryByDate = emptyMap()
    }
}

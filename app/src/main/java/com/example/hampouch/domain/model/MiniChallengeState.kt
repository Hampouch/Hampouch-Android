package com.example.hampouch.domain.model

import java.time.LocalDate

/**
 * 미니 챌린지 도메인의 전체 상태.
 *
 * @property summaryByDate 서버 모드에서만 채워진다. 목데이터 모드에서는 비어 있고 화면이 개별 항목으로부터 근사 계산한다.
 */
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

/** 중복 비교용 정규화 — 앞뒤·중간 공백과 대소문자를 무시한다. */
fun normalizeMiniChallengeName(name: String): String =
    name.replace(Regex("\\s+"), "").lowercase()

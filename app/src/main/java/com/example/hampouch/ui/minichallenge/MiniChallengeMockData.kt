package com.example.hampouch.ui.minichallenge

import com.example.hampouch.data.model.MiniChallengeEntry
import com.example.hampouch.data.model.RecommendedMiniChallenge

object MiniChallengeMockData {

    const val streakDays = 3

    fun todayChallenges(): List<MiniChallengeEntry> = listOf(
        MiniChallengeEntry(id = "m1", name = "커피 사먹지 않기", periodLabel = "3/7일", isChecked = false),
        MiniChallengeEntry(id = "m2", name = "배달 음식 참기", periodLabel = "3일간", isChecked = false),
        MiniChallengeEntry(id = "m3", name = "저녁 집밥 먹기", periodLabel = "7일간", isChecked = false),
        MiniChallengeEntry(id = "m4", name = "물 많이 마시기", periodLabel = "오늘만", isChecked = false)
    )

    fun recommendedChallenges(): List<RecommendedMiniChallenge> = listOf(
        RecommendedMiniChallenge(id = "r1", periodLabel = "오늘만", name = "편의점 디저트 안 먹기"),
        RecommendedMiniChallenge(id = "r2", periodLabel = "3일간", name = "배달 음식 참기"),
        RecommendedMiniChallenge(id = "r3", periodLabel = "7일간", name = "택시 대신 대중교통 이용하기"),
        RecommendedMiniChallenge(id = "r4", periodLabel = "오늘만", name = "저녁 집밥 먹기")
    )
}

package com.example.hampouch.ui.minichallenge

import com.example.hampouch.domain.model.MiniChallengeEntry
import com.example.hampouch.domain.model.RecommendedMiniChallenge

object MiniChallengeMockData {

    fun todayChallenges(): List<MiniChallengeEntry> = listOf(
        MiniChallengeEntry(id = "m1", name = "커피 사먹지 않기", totalDays = 7, achievedDays = 3, isChecked = false),
        MiniChallengeEntry(id = "m2", name = "배달 음식 참기", totalDays = 3, achievedDays = 0, isChecked = false),
        MiniChallengeEntry(id = "m3", name = "저녁 집밥 먹기", totalDays = 7, achievedDays = 0, isChecked = false),
        MiniChallengeEntry(id = "m4", name = "물 많이 마시기", totalDays = null, achievedDays = 0, isChecked = false)
    )

    fun yesterdayChallenges(): List<MiniChallengeEntry> = listOf(
        MiniChallengeEntry(id = "y1", name = "커피 사먹지 않기", totalDays = 7, achievedDays = 4, isChecked = true),
        MiniChallengeEntry(id = "y2", name = "배달 음식 참기", totalDays = 7, achievedDays = 3, isChecked = true),
        MiniChallengeEntry(id = "y3", name = "저녁 집밥 먹기", totalDays = null, achievedDays = 0, isChecked = false)
    )

    fun recommendedChallenges(): List<RecommendedMiniChallenge> = listOf(
        RecommendedMiniChallenge(id = "r1", totalDays = null, name = "편의점 디저트 안 먹기"),
        RecommendedMiniChallenge(id = "r2", totalDays = 3, name = "배달 음식 참기"),
        RecommendedMiniChallenge(id = "r3", totalDays = 7, name = "택시 대신 대중교통 이용하기"),
        RecommendedMiniChallenge(id = "r4", totalDays = null, name = "저녁 집밥 먹기")
    )
}

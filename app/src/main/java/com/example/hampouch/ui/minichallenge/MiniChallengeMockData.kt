package com.example.hampouch.ui.minichallenge

import com.example.hampouch.domain.model.MiniChallengeEntry
import com.example.hampouch.domain.model.RecommendedMiniChallenge
import com.example.hampouch.domain.model.MiniChallengeDuration
import com.example.hampouch.domain.model.miniChallengeDuration

object MiniChallengeMockData {

    fun todayChallenges(): List<MiniChallengeEntry> = listOf(
        MiniChallengeEntry(id = "m1", name = "커피 사먹지 않기", duration = miniChallengeDuration(7), achievedDays = 3, isChecked = false),
        MiniChallengeEntry(id = "m2", name = "배달 음식 참기", duration = miniChallengeDuration(3), achievedDays = 0, isChecked = false),
        MiniChallengeEntry(id = "m3", name = "저녁 집밥 먹기", duration = miniChallengeDuration(7), achievedDays = 0, isChecked = false),
        MiniChallengeEntry(id = "m4", name = "물 많이 마시기", duration = MiniChallengeDuration.Today, achievedDays = 0, isChecked = false)
    )

    fun yesterdayChallenges(): List<MiniChallengeEntry> = listOf(
        MiniChallengeEntry(id = "y1", name = "커피 사먹지 않기", duration = miniChallengeDuration(7), achievedDays = 4, isChecked = true),
        MiniChallengeEntry(id = "y2", name = "배달 음식 참기", duration = miniChallengeDuration(7), achievedDays = 3, isChecked = true),
        MiniChallengeEntry(id = "y3", name = "저녁 집밥 먹기", duration = MiniChallengeDuration.Today, achievedDays = 0, isChecked = false)
    )

    fun recommendedChallenges(): List<RecommendedMiniChallenge> = listOf(
        RecommendedMiniChallenge(id = "r1", duration = MiniChallengeDuration.Today, name = "편의점 디저트 안 먹기"),
        RecommendedMiniChallenge(id = "r2", duration = miniChallengeDuration(3), name = "배달 음식 참기"),
        RecommendedMiniChallenge(id = "r3", duration = miniChallengeDuration(7), name = "택시 대신 대중교통 이용하기"),
        RecommendedMiniChallenge(id = "r4", duration = MiniChallengeDuration.Today, name = "저녁 집밥 먹기")
    )
}

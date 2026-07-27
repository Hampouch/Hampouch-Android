package com.example.hampouch.ui.mypage

import com.example.hampouch.data.model.ChallengeRecord
import com.example.hampouch.data.model.ChallengeStatus
import com.example.hampouch.data.model.MyPageProfile
import com.example.hampouch.data.model.TipPost
import com.example.hampouch.ui.hamtips.HamTipsRepository

object MyPageMockData {

    fun defaultProfile(): MyPageProfile = MyPageProfile(
        name = "민준",
        handle = "hampochi_minjun",
        email = "hampouch@example.com"
    )

    fun challengeHistory(): List<ChallengeRecord> = listOf(
        ChallengeRecord(
            id = "challenge_ongoing",
            status = ChallengeStatus.IN_PROGRESS,
            totalDays = 14,
            achievedDays = 2,
            startDateLabel = "2026.05.15.",
            endDateLabel = null,
            dailyLimit = 27_000,
            totalSaved = 5_400
        ),
        ChallengeRecord(
            id = "challenge_success",
            status = ChallengeStatus.SUCCESS,
            totalDays = 14,
            achievedDays = 14,
            startDateLabel = "2026.05.01.",
            endDateLabel = "2026.05.14.",
            dailyLimit = 25_000,
            totalSaved = 21_400
        ),
        ChallengeRecord(
            id = "challenge_fail",
            status = ChallengeStatus.FAIL,
            totalDays = 14,
            achievedDays = 3,
            startDateLabel = "2026.04.01.",
            endDateLabel = "2026.04.14.",
            dailyLimit = 30_000,
            totalSaved = 2_400
        )
    )

    fun emptyChallengeHistory(): List<ChallengeRecord> = emptyList()

    fun myTips(): List<TipPost> = HamTipsRepository.allPosts.filter { it.authorId == HamTipsRepository.CURRENT_USER_ID }

    fun savedTips(): List<TipPost> = HamTipsRepository.allPosts.filter { it.isSaved }

    fun emptyTips(): List<TipPost> = emptyList()
}

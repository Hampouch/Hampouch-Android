package com.example.hampouch.ui.mypage

import com.example.hampouch.data.model.ChallengeRecord
import com.example.hampouch.data.model.ChallengeStatus
import com.example.hampouch.data.model.MyPageProfile
import com.example.hampouch.data.model.TipPost
import com.example.hampouch.ui.hambattle.HamBattleMockData
import com.example.hampouch.ui.hamtips.HamTipsRepository

object MyPageMockData {

    private const val HAMBATTLE_CURRENT_USER_NAME = "나"
    private val hamBattlePeriodRegex = Regex(
        """(\d{2})\.(\d{2})\.(\d{2}) - (\d{2})\.(\d{2})\.(\d{2}) \((\d+)일\)"""
    )

    private val takenNicknames = listOf("햄포치")

    fun defaultProfile(): MyPageProfile = MyPageProfile(
        name = "절약왕 민준",
        handle = "hampochi_minjun",
        email = "hampouch@example.com"
    )

    fun isNicknameTaken(name: String): Boolean = name in takenNicknames

    fun challengeHistory(): List<ChallengeRecord> = HamBattleMockData.endedChallenges.mapNotNull { challenge ->
        val me = challenge.participants.find { it.name == HAMBATTLE_CURRENT_USER_NAME } ?: return@mapNotNull null
        val period = hamBattlePeriodRegex.find(challenge.periodLabel) ?: return@mapNotNull null
        val (startYY, startMM, startDD, endYY, endMM, endDD, totalDaysText) = period.destructured
        val totalDays = totalDaysText.toInt()

        val ranked = challenge.participants.sortedBy { it.amount }
        val isWinner = ranked.firstOrNull()?.name == HAMBATTLE_CURRENT_USER_NAME
        val otherAmounts = challenge.participants
            .filter { it.name != HAMBATTLE_CURRENT_USER_NAME }
            .map { it.amount }
        val averageOthersAmount = if (otherAmounts.isNotEmpty()) otherAmounts.average() else me.amount.toDouble()

        ChallengeRecord(
            id = challenge.id,
            status = if (isWinner) ChallengeStatus.SUCCESS else ChallengeStatus.FAIL,
            totalDays = totalDays,
            startDateLabel = "20$startYY.$startMM.$startDD.",
            endDateLabel = "20$endYY.$endMM.$endDD.",
            targetAmount = averageOthersAmount.toInt(),
            actualAmount = me.amount
        )
    }

    fun emptyChallengeHistory(): List<ChallengeRecord> = emptyList()

    fun myTips(): List<TipPost> = HamTipsRepository.allPosts.filter { it.authorId == HamTipsRepository.CURRENT_USER_ID }

    fun savedTips(): List<TipPost> = HamTipsRepository.allPosts.filter { it.isSaved }

    fun emptyTips(): List<TipPost> = emptyList()
}

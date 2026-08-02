package com.example.hampouch.ui.mypage

import com.example.hampouch.data.model.ChallengeRecord
import com.example.hampouch.data.model.ChallengeStatus
import com.example.hampouch.data.model.MyPageProfile
import com.example.hampouch.data.model.TipPost
import com.example.hampouch.ui.hambattle.HamBattleMockData
import com.example.hampouch.ui.hamtips.HamTipsRepository
import com.example.hampouch.ui.session.UserSession

object MyPageMockData {

    private const val HAMBATTLE_CURRENT_USER_NAME = "나"
    private val hamBattlePeriodRegex = Regex(
        """(\d{2})\.(\d{2})\.(\d{2}) - (\d{2})\.(\d{2})\.(\d{2}) \((\d+)일\)"""
    )

    private val takenNicknames = listOf("햄포치")

    // 로그인 세션(UserSession)과 동일한 계정 정보를 표시해 화면 간 사용자 이름/이메일 불일치를 없앤다.
    fun defaultProfile(): MyPageProfile {
        val user = UserSession.currentUser
        return MyPageProfile(
            name = user.name,
            handle = "hampochi_${user.id}",
            email = user.email
        )
    }

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

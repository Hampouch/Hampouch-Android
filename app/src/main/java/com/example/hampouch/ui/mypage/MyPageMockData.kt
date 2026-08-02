package com.example.hampouch.ui.mypage

import com.example.hampouch.data.model.ChallengeRecord
import com.example.hampouch.data.model.ChallengeStatus
import com.example.hampouch.data.model.MyPageProfile
import com.example.hampouch.data.model.TipPost
import com.example.hampouch.data.repository.ChallengeRepository
import com.example.hampouch.ui.expensedetail.ExpenseDetailStore
import com.example.hampouch.ui.hamtips.HamTipsRepository
import com.example.hampouch.ui.session.UserSession
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object MyPageMockData {

    private val takenNicknames = listOf("햄포치")
    private val challengeHistoryPeriodFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.")

    fun defaultProfile(): MyPageProfile {
        val user = UserSession.currentUser
        return MyPageProfile(
            name = user.name,
            handle = "hampochi_${user.id}",
            email = user.email
        )
    }

    fun isNicknameTaken(name: String): Boolean = name in takenNicknames

    fun challengeHistory(): List<ChallengeRecord> {
        val active = ChallengeRepository.activeChallenge
        val referenceToday = LocalDate.now()
        val targetAmount = active.dailyLimit * active.totalDays

        val elapsedDays = ChallengeRepository.elapsedDays(referenceToday)
        val activeActualAmount = elapsedDays.sumOf { day -> ExpenseDetailStore.recordsForDate(day).sumOf { it.amount } }
        val activeRecord = ChallengeRecord(
            id = "active_challenge",
            status = ChallengeStatus.IN_PROGRESS,
            totalDays = active.totalDays,
            startDateLabel = active.periodStart.format(challengeHistoryPeriodFormatter),
            endDateLabel = null,
            targetAmount = targetAmount,
            actualAmount = activeActualAmount
        )

        val previous = ChallengeRepository.previousChallenge
        val previousTargetAmount = previous.dailyLimit * previous.totalDays
        val pastActualAmount = generateSequence(previous.periodStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(previous.periodEnd) }
            .sumOf { day -> ExpenseDetailStore.recordsForDate(day).sumOf { it.amount } }
        val pastRecord = ChallengeRecord(
            id = "past_challenge_1",
            status = if (pastActualAmount <= previousTargetAmount) ChallengeStatus.SUCCESS else ChallengeStatus.FAIL,
            totalDays = previous.totalDays,
            startDateLabel = previous.periodStart.format(challengeHistoryPeriodFormatter),
            endDateLabel = previous.periodEnd.format(challengeHistoryPeriodFormatter),
            targetAmount = previousTargetAmount,
            actualAmount = pastActualAmount
        )

        return listOf(activeRecord, pastRecord)
    }

    fun emptyChallengeHistory(): List<ChallengeRecord> = emptyList()

    fun myTips(): List<TipPost> = HamTipsRepository.allPosts.filter { it.authorId == HamTipsRepository.CURRENT_USER_ID }

    fun savedTips(): List<TipPost> = HamTipsRepository.allPosts.filter { it.isSaved }

    fun emptyTips(): List<TipPost> = emptyList()
}

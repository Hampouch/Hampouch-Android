package com.example.hampouch.ui.mypage

import com.example.hampouch.domain.model.ActiveChallenge
import com.example.hampouch.domain.model.ChallengeRecord
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.ChallengeStatus
import com.example.hampouch.domain.model.MyPageProfile
import com.example.hampouch.domain.model.User
import com.example.hampouch.domain.model.TipPost
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object MyPageMockData {

    private val challengeHistoryPeriodFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.")

    fun defaultProfile(user: User): MyPageProfile {
        return MyPageProfile(
            name = user.name,
            handle = user.email,
            email = user.email
        )
    }

    fun challengeHistory(challengeState: ChallengeState, spentOnDate: (LocalDate) -> Int): List<ChallengeRecord> {
        val referenceToday = LocalDate.now()
        return challengeState.challenges
            .sortedWith(
                compareByDescending<ActiveChallenge> { it.id.toLongOrNull() ?: Long.MIN_VALUE }
                    .thenByDescending { it.periodStart }
            )
            .map { challenge ->
                val isOngoing = challenge.isOngoingOn(referenceToday)
                val trackedEnd = if (referenceToday.isBefore(challenge.effectivePeriodEnd)) referenceToday else challenge.effectivePeriodEnd
                val actualAmount = generateSequence(challenge.periodStart) { it.plusDays(1) }
                    .takeWhile { !it.isAfter(trackedEnd) }
                    .sumOf { day -> spentOnDate(day) }
                ChallengeRecord(
                    id = challenge.id,
                    status = when {
                        challenge.abandonedDate != null -> ChallengeStatus.FAIL
                        challenge.remoteStatus == "FAIL" -> ChallengeStatus.FAIL
                        isOngoing -> ChallengeStatus.IN_PROGRESS
                        challenge.remoteStatus == "SUCCESS" -> ChallengeStatus.SUCCESS
                        challenge.remoteStatus != null -> ChallengeStatus.FAIL
                        actualAmount <= challenge.targetAmount -> ChallengeStatus.SUCCESS
                        else -> ChallengeStatus.FAIL
                    },
                    totalDays = challenge.totalDays,
                    startDateLabel = challenge.periodStart.format(challengeHistoryPeriodFormatter),
                    endDateLabel = challenge.periodEnd.format(challengeHistoryPeriodFormatter),
                    targetAmount = challenge.targetAmount,
                    actualAmount = actualAmount
                )
            }
    }

    fun emptyChallengeHistory(): List<ChallengeRecord> = emptyList()

    fun myTips(posts: List<TipPost>, userId: String): List<TipPost> =
        posts.filter { it.authorId == userId }.sortedBy { it.postedMinutesAgo }

    fun savedTips(posts: List<TipPost>): List<TipPost> =
        posts.filter { it.isSaved }.sortedBy { it.postedMinutesAgo }

    fun emptyTips(): List<TipPost> = emptyList()
}

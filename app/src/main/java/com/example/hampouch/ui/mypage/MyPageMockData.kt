package com.example.hampouch.ui.mypage

import com.example.hampouch.domain.model.ChallengeRecord
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.ChallengeStatus
import com.example.hampouch.domain.model.MyPageProfile
import com.example.hampouch.domain.model.User
import com.example.hampouch.domain.model.TipPost
import com.example.hampouch.data.repository.HamTipsRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object MyPageMockData {

    private val takenNicknames = listOf("햄포치")
    private val challengeHistoryPeriodFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.")

    fun defaultProfile(user: User): MyPageProfile {
        return MyPageProfile(
            name = user.name,
            handle = user.email,
            email = user.email
        )
    }

    fun isNicknameTaken(name: String): Boolean = name in takenNicknames

    /** @param spentOnDate 해당 날짜의 지출 합계. */
    fun challengeHistory(challengeState: ChallengeState, spentOnDate: (LocalDate) -> Int): List<ChallengeRecord> {
        val referenceToday = LocalDate.now()
        return challengeState.challenges
            .sortedByDescending { it.periodStart }
            .map { challenge ->
                val isOngoing = challenge.id == challengeState.activeChallenge?.id &&
                    !referenceToday.isAfter(challenge.effectivePeriodEnd)
                val trackedEnd = if (referenceToday.isBefore(challenge.effectivePeriodEnd)) referenceToday else challenge.effectivePeriodEnd
                val actualAmount = generateSequence(challenge.periodStart) { it.plusDays(1) }
                    .takeWhile { !it.isAfter(trackedEnd) }
                    .sumOf { day -> spentOnDate(day) }
                ChallengeRecord(
                    id = challenge.id,
                    status = when {
                        challenge.remoteStatus == "SUCCESS" -> ChallengeStatus.SUCCESS
                        challenge.remoteStatus == "FAIL" -> ChallengeStatus.FAIL
                        challenge.abandonedDate != null -> ChallengeStatus.FAIL
                        isOngoing -> ChallengeStatus.IN_PROGRESS
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

    fun myTips(userId: String): List<TipPost> = HamTipsRepository.allPosts.filter { it.authorId == userId }

    fun savedTips(): List<TipPost> = HamTipsRepository.allPosts.filter { it.isSaved }

    fun emptyTips(): List<TipPost> = emptyList()
}

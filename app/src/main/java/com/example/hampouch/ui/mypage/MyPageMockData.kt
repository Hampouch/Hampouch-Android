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

    /** @param spentOnDate 해당 날짜의 지출 합계. */
    fun challengeHistory(challengeState: ChallengeState, spentOnDate: (LocalDate) -> Int): List<ChallengeRecord> {
        val referenceToday = LocalDate.now()
        return challengeState.challenges
            // 서버 challengeId는 생성 시마다 증가하는 값이라 실제 생성순을 그대로 반영한다.
            // periodStart만으로는 같은 날 포기 후 재생성된 챌린지의 순서를 구분할 수 없고(둘 다
            // 오늘 시작), 로컬 리스트에 항목이 쌓인 순서는 API 호출 타이밍에 따라 달라져 신뢰할 수
            // 없으므로, id를 우선 키로 사용하고 서버 id가 없는 로컬/목업 챌린지만 periodStart로 정렬한다.
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

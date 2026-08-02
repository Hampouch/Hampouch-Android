package com.example.hampouch.data.repository

import androidx.compose.runtime.mutableStateOf
import com.example.hampouch.data.model.ActiveChallenge
import com.example.hampouch.data.model.ChallengeProgress
import com.example.hampouch.data.model.DailyRecordStatus
import java.time.LocalDate

// 서버팀 API 연동 전까지 화면 간 "현재 진행중인 챌린지" 정보를 일치시키는 단일 소스.
// TODO: 서버팀 챌린지 API 연동 시 이 목데이터 대신 data/remote/ApiService 호출 결과로 activeChallengeState를 채우도록 교체.
object ChallengeRepository {

    private const val CHALLENGE_TOTAL_DAYS = 14
    private const val CHALLENGE_DAY_OF_PROGRESS = 7
    private const val CHALLENGE_DAILY_LIMIT = 20_000
    private const val CHALLENGE_SAVED_AMOUNT = 21_400
    private const val CHALLENGE_STREAK_DAYS = 4

    private fun buildInitialChallenge(): ActiveChallenge {
        val today = LocalDate.now()
        val start = today.minusDays((CHALLENGE_DAY_OF_PROGRESS - 1).toLong())
        val end = start.plusDays((CHALLENGE_TOTAL_DAYS - 1).toLong())
        return ActiveChallenge(
            id = "challenge_active",
            totalDays = CHALLENGE_TOTAL_DAYS,
            periodStart = start,
            periodEnd = end,
            dailyLimit = CHALLENGE_DAILY_LIMIT,
            targetAmount = CHALLENGE_DAILY_LIMIT * CHALLENGE_TOTAL_DAYS,
            savedAmount = CHALLENGE_SAVED_AMOUNT,
            streakDays = CHALLENGE_STREAK_DAYS,
            editCount = 0
        )
    }

    private val activeChallengeState = mutableStateOf(buildInitialChallenge())

    val activeChallenge: ActiveChallenge
        get() = activeChallengeState.value

    fun updateTargetAmount(newTargetAmount: Int) {
        val current = activeChallengeState.value
        activeChallengeState.value = current.copy(
            targetAmount = newTargetAmount,
            dailyLimit = (newTargetAmount / current.totalDays).coerceAtLeast(0),
            editCount = current.editCount + 1
        )
    }

    // 챌린지 시작일부터 기준일(또는 챌린지 종료일 중 이른 쪽)까지의 날짜 목록.
    fun elapsedDays(referenceToday: LocalDate): List<LocalDate> {
        val active = activeChallenge
        val trackedEnd = if (referenceToday.isBefore(active.periodEnd)) referenceToday else active.periodEnd
        return generateSequence(active.periodStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(trackedEnd) }
            .toList()
    }

    // spentOnDate: 해당 날짜의 실제 지출 합계를 반환하는 함수(호출부에서 ExpenseDetailStore를 주입).
    // 홈 화면과 챌린지 결과 화면이 동일한 로직으로 절약 금액/연속 달성/일별 성공 여부를 계산하도록 공유한다.
    fun computeProgress(referenceToday: LocalDate, spentOnDate: (LocalDate) -> Int): ChallengeProgress {
        val dailyLimit = activeChallenge.dailyLimit
        val days = elapsedDays(referenceToday)
        fun balanceOn(date: LocalDate) = dailyLimit - spentOnDate(date)

        val savedAmount = days.sumOf { balanceOn(it) }
        var streakDays = 0
        for (day in days.asReversed()) {
            if (balanceOn(day) >= 0) streakDays++ else break
        }
        val dailyRecords = days.associateWith { day ->
            if (balanceOn(day) >= 0) DailyRecordStatus.SUCCESS else DailyRecordStatus.FAIL
        }
        return ChallengeProgress(savedAmount = savedAmount, streakDays = streakDays, dailyRecords = dailyRecords)
    }
}

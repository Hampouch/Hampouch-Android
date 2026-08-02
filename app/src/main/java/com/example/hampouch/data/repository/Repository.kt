package com.example.hampouch.data.repository

import androidx.compose.runtime.mutableStateOf
import com.example.hampouch.data.model.ActiveChallenge
import com.example.hampouch.data.model.ChallengeProgress
import com.example.hampouch.data.model.DailyRecordStatus
import java.time.LocalDate

// TODO: 서버팀 챌린지 API 연동 시 이 목데이터 대신 data/remote/ApiService 호출 결과로 activeChallengeState를 채우도록 교체.
object ChallengeRepository {

    private const val CHALLENGE_TOTAL_DAYS = 14
    private const val CHALLENGE_DAY_OF_PROGRESS = 7
    private const val CHALLENGE_DAILY_LIMIT = 20_000
    private const val CHALLENGE_SAVED_AMOUNT = 21_400
    private const val CHALLENGE_STREAK_DAYS = 4

    private const val PREVIOUS_CHALLENGE_TOTAL_DAYS = 7
    private const val PREVIOUS_CHALLENGE_DAILY_LIMIT = 20_000

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

    val previousChallenge: ActiveChallenge
        get() {
            val active = activeChallenge
            val end = active.periodStart.minusDays(1)
            val start = end.minusDays((PREVIOUS_CHALLENGE_TOTAL_DAYS - 1).toLong())
            return ActiveChallenge(
                id = "challenge_previous",
                totalDays = PREVIOUS_CHALLENGE_TOTAL_DAYS,
                periodStart = start,
                periodEnd = end,
                dailyLimit = PREVIOUS_CHALLENGE_DAILY_LIMIT,
                targetAmount = PREVIOUS_CHALLENGE_DAILY_LIMIT * PREVIOUS_CHALLENGE_TOTAL_DAYS,
                savedAmount = 0,
                streakDays = 0,
                editCount = 0
            )
        }

    fun challengeFor(date: LocalDate): ActiveChallenge? {
        val active = activeChallenge
        if (!date.isBefore(active.periodStart)) return active
        val previous = previousChallenge
        if (!date.isBefore(previous.periodStart) && !date.isAfter(previous.periodEnd)) return previous
        return null
    }

    fun updateTargetAmount(newTargetAmount: Int) {
        val current = activeChallengeState.value
        activeChallengeState.value = current.copy(
            targetAmount = newTargetAmount,
            dailyLimit = (newTargetAmount / current.totalDays).coerceAtLeast(0),
            editCount = current.editCount + 1
        )
    }

    fun elapsedDays(referenceToday: LocalDate, challenge: ActiveChallenge = activeChallenge): List<LocalDate> {
        val trackedEnd = if (referenceToday.isBefore(challenge.periodEnd)) referenceToday else challenge.periodEnd
        return generateSequence(challenge.periodStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(trackedEnd) }
            .toList()
    }

    fun computeProgress(
        referenceToday: LocalDate,
        challenge: ActiveChallenge = activeChallenge,
        spentOnDate: (LocalDate) -> Int
    ): ChallengeProgress {
        val dailyLimit = challenge.dailyLimit
        val days = elapsedDays(referenceToday, challenge)
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

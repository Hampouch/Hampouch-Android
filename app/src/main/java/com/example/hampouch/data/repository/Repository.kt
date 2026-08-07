package com.example.hampouch.data.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.hampouch.data.model.ActiveChallenge
import com.example.hampouch.data.model.ChallengeProgress
import com.example.hampouch.data.model.DailyLimitOverride
import com.example.hampouch.data.model.DailyRecordStatus
import com.example.hampouch.data.model.OnboardingRequest
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// TODO: 서버팀 챌린지 API 연동 시 이 목데이터 대신 data/remote/ApiService 호출 결과로 challengesState를 채우도록 교체.
object ChallengeRepository {

    private const val CHALLENGE_TOTAL_DAYS = 14
    private const val CHALLENGE_DAY_OF_PROGRESS = 7
    private const val CHALLENGE_DAILY_LIMIT = 20_000
    private const val CHALLENGE_SAVED_AMOUNT = 21_400
    private const val CHALLENGE_STREAK_DAYS = 4

    private const val PREVIOUS_CHALLENGE_TOTAL_DAYS = 7
    private const val PREVIOUS_CHALLENGE_DAILY_LIMIT = 20_000

    private const val DEFAULT_ONE_OFF_DAYS = 30

    private fun buildSeedChallenges(referenceToday: LocalDate): List<ActiveChallenge> {
        val activeStart = referenceToday.minusDays((CHALLENGE_DAY_OF_PROGRESS - 1).toLong())
        val activeEnd = activeStart.plusDays((CHALLENGE_TOTAL_DAYS - 1).toLong())
        val previousEnd = activeStart.minusDays(1)
        val previousStart = previousEnd.minusDays((PREVIOUS_CHALLENGE_TOTAL_DAYS - 1).toLong())

        val previous = ActiveChallenge(
            id = "challenge_previous",
            totalDays = PREVIOUS_CHALLENGE_TOTAL_DAYS,
            periodStart = previousStart,
            periodEnd = previousEnd,
            dailyLimit = PREVIOUS_CHALLENGE_DAILY_LIMIT,
            targetAmount = PREVIOUS_CHALLENGE_DAILY_LIMIT * PREVIOUS_CHALLENGE_TOTAL_DAYS,
            savedAmount = 0,
            streakDays = 0,
            editCount = 0
        )
        val active = ActiveChallenge(
            id = "challenge_active",
            totalDays = CHALLENGE_TOTAL_DAYS,
            periodStart = activeStart,
            periodEnd = activeEnd,
            dailyLimit = CHALLENGE_DAILY_LIMIT,
            targetAmount = CHALLENGE_DAILY_LIMIT * CHALLENGE_TOTAL_DAYS,
            savedAmount = CHALLENGE_SAVED_AMOUNT,
            streakDays = CHALLENGE_STREAK_DAYS,
            editCount = 0
        )
        return listOf(previous, active)
    }

    private val challengesState = mutableStateOf(buildSeedChallenges(LocalDate.now()))

    val challenges: List<ActiveChallenge>
        get() = challengesState.value

    val activeChallenge: ActiveChallenge
        get() = challenges.last()

    val hasOngoingChallenge: Boolean
        get() = !LocalDate.now().isAfter(activeChallenge.periodEnd)

    var challengeEndAcknowledged: Boolean by mutableStateOf(false)
        private set

    var hasVisitedExpenseEditAfterEnd: Boolean by mutableStateOf(false)
        private set

    fun isChallengeJustEnded(referenceToday: LocalDate): Boolean =
        !challengeEndAcknowledged && referenceToday.isAfter(activeChallenge.periodEnd)

    fun markVisitedExpenseEditAfterEnd() {
        hasVisitedExpenseEditAfterEnd = true
    }

    fun acknowledgeChallengeEnd() {
        val ended = activeChallenge
        if (ended.repeatMonthly) {
            val nextStart = ended.periodStart.plusMonths(1)
            val nextEnd = nextStart.plusMonths(1).minusDays(1)
            val nextTotalDays = ChronoUnit.DAYS.between(nextStart, nextEnd).toInt() + 1
            val nextChallenge = ActiveChallenge(
                id = "challenge_${System.currentTimeMillis()}",
                totalDays = nextTotalDays,
                periodStart = nextStart,
                periodEnd = nextEnd,
                dailyLimit = ended.dailyLimit,
                targetAmount = ended.targetAmount,
                savedAmount = 0,
                streakDays = 0,
                editCount = 0,
                repeatMonthly = true
            )
            challengesState.value = challenges + nextChallenge
            hasVisitedExpenseEditAfterEnd = false
        } else {
            challengeEndAcknowledged = true
        }
    }

    fun startNewChallenge(request: OnboardingRequest, referenceToday: LocalDate = LocalDate.now()): ActiveChallenge {
        val (periodStart, periodEnd, repeatMonthly) = if (request.dateFixed) {
            val anchor = request.startDate ?: referenceToday
            val start = if (anchor.isBefore(referenceToday)) referenceToday else anchor
            Triple(start, start.plusMonths(1).minusDays(1), true)
        } else {
            val days = (request.customPeriodDays ?: DEFAULT_ONE_OFF_DAYS).coerceAtLeast(1)
            Triple(referenceToday, referenceToday.plusDays((days - 1).toLong()), false)
        }
        val totalDays = ChronoUnit.DAYS.between(periodStart, periodEnd).toInt() + 1
        val targetAmount = (request.totalTargetAmount ?: 0).coerceAtLeast(0)
        val dailyLimit = (request.dailyTargetAmount ?: (if (totalDays > 0) targetAmount / totalDays else 0))
            .coerceAtLeast(0)

        val newChallenge = ActiveChallenge(
            id = "challenge_${System.currentTimeMillis()}",
            totalDays = totalDays,
            periodStart = periodStart,
            periodEnd = periodEnd,
            dailyLimit = dailyLimit,
            targetAmount = targetAmount,
            savedAmount = 0,
            streakDays = 0,
            editCount = 0,
            repeatMonthly = repeatMonthly
        )
        challengesState.value = challenges + newChallenge
        challengeEndAcknowledged = false
        hasVisitedExpenseEditAfterEnd = false
        return newChallenge
    }

    fun challengeFor(date: LocalDate): ActiveChallenge? =
        challenges.firstOrNull { !date.isBefore(it.periodStart) && !date.isAfter(it.periodEnd) }

    fun updateTargetAmount(newTargetAmount: Int, effectiveFrom: LocalDate = LocalDate.now()) {
        val current = activeChallenge
        val newDailyLimit = (newTargetAmount / current.totalDays).coerceAtLeast(0)
        val updated = current.copy(
            targetAmount = newTargetAmount,
            dailyLimit = newDailyLimit,
            editCount = current.editCount + 1,
            dailyLimitOverrides = current.dailyLimitOverrides + DailyLimitOverride(effectiveFrom, newDailyLimit)
        )
        challengesState.value = challenges.dropLast(1) + updated
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
        val days = elapsedDays(referenceToday, challenge)
        fun balanceOn(date: LocalDate) = challenge.dailyLimitOn(date) - spentOnDate(date)

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

    fun resetForAccount(referenceToday: LocalDate = LocalDate.now()) {
        challengesState.value = buildSeedChallenges(referenceToday)
        challengeEndAcknowledged = false
        hasVisitedExpenseEditAfterEnd = false
    }
}

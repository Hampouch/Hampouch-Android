package com.example.hampouch.ui.challengeresult

import com.example.hampouch.data.model.ChallengeResultStatus
import com.example.hampouch.data.model.ChallengeResultUiState
import com.example.hampouch.data.model.DailyRecordStatus
import com.example.hampouch.data.model.DailyRecordStatus.FAIL
import com.example.hampouch.data.model.DailyRecordStatus.SUCCESS
import com.example.hampouch.data.model.EmotionStat
import com.example.hampouch.data.model.SpendingEmotion
import com.example.hampouch.data.repository.ChallengeRepository
import java.time.LocalDate
import java.time.YearMonth

object ChallengeResultMockData {

    private val challengeMonth = YearMonth.of(2026, 5)

    private val emotionStats = listOf(
        EmotionStat(SpendingEmotion.CRAVING, 42),
        EmotionStat(SpendingEmotion.STRESS, 25),
        EmotionStat(SpendingEmotion.LAZY, 15),
        EmotionStat(SpendingEmotion.REWARD, 10),
        EmotionStat(SpendingEmotion.ETC, 8)
    )

    private fun mayDay(day: Int): LocalDate = challengeMonth.atDay(day)

    private fun recordsOf(vararg days: Pair<Int, DailyRecordStatus>): Map<LocalDate, DailyRecordStatus> =
        days.associate { (day, status) -> mayDay(day) to status }

    val complete = ChallengeResultUiState(
        status = ChallengeResultStatus.COMPLETE,
        title = "14일 챌린지",
        periodStart = mayDay(1),
        periodEnd = mayDay(14),
        totalDays = 14,
        successDays = 14,
        streakDays = 14,
        amountLabel = "총 절약",
        amountValue = 27_500,
        goalAmount = 400_000,
        actualAmount = 372_500,
        dailyLimit = 25_000,
        emotionStats = emotionStats,
        dailyRecords = recordsOf(
            *(1..14).map { it to SUCCESS }.toTypedArray()
        )
    )

    val fail = ChallengeResultUiState(
        status = ChallengeResultStatus.FAIL,
        title = "14일 챌린지",
        periodStart = mayDay(1),
        periodEnd = mayDay(14),
        totalDays = 14,
        successDays = 9,
        streakDays = 4,
        amountLabel = "초과 금액",
        amountValue = 24_100,
        goalAmount = 400_000,
        actualAmount = 424_100,
        dailyLimit = 25_000,
        emotionStats = emotionStats,
        dailyRecords = recordsOf(
            1 to SUCCESS, 2 to SUCCESS, 3 to FAIL, 4 to SUCCESS, 5 to FAIL,
            6 to SUCCESS, 7 to FAIL, 8 to SUCCESS, 9 to SUCCESS, 10 to SUCCESS,
            11 to FAIL, 12 to SUCCESS, 13 to FAIL, 14 to SUCCESS
        )
    )

    // 현재 진행중인 챌린지: 홈/금액조정/지출입력과 동일한 ChallengeRepository를 참조해 숫자를 일치시킨다.
    fun inProgress(): ChallengeResultUiState {
        val active = ChallengeRepository.activeChallenge
        val successDays = active.streakDays.coerceIn(1, active.totalDays)
        return ChallengeResultUiState(
            status = ChallengeResultStatus.IN_PROGRESS,
            title = "${active.totalDays}일 챌린지",
            periodStart = active.periodStart,
            periodEnd = active.periodEnd,
            totalDays = active.totalDays,
            successDays = successDays,
            streakDays = active.streakDays,
            amountLabel = "총 절약",
            amountValue = active.savedAmount,
            goalAmount = active.targetAmount,
            actualAmount = (active.targetAmount - active.savedAmount).coerceAtLeast(0),
            dailyLimit = active.dailyLimit,
            emotionStats = emotionStats,
            dailyRecords = (1..successDays).associate { day ->
                active.periodStart.plusDays((day - 1).toLong()) to SUCCESS
            }
        )
    }
}

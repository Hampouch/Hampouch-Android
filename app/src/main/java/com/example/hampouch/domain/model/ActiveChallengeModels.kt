package com.example.hampouch.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class DailyLimitOverride(
    val effectiveFrom: LocalDate,
    val dailyLimit: Int
)

data class ActiveChallenge(
    val id: String,
    val totalDays: Int,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val dailyLimit: Int,
    val targetAmount: Int,
    val savedAmount: Int,
    val streakDays: Int,
    val editCount: Int,
    val repeatMonthly: Boolean = false,
    val dailyLimitOverrides: List<DailyLimitOverride> = listOf(DailyLimitOverride(periodStart, dailyLimit)),
    val abandonedDate: LocalDate? = null,
    val remoteStatus: String? = null,
    val expenseLockedAt: String? = null,
    val resultSummary: ChallengeResultSummary? = null,
    val emotionBreakdown: List<EmotionStat> = emptyList(),
    val calendarDays: Map<LocalDate, DailyRecordStatus> = emptyMap(),
    val warningCodes: List<String> = emptyList()
) {
    val maxEditCount: Int
        get() = if (totalDays >= 15) 2 else 1

    val canEditTarget: Boolean
        get() = editCount < maxEditCount

    val effectivePeriodEnd: LocalDate
        get() = abandonedDate?.minusDays(1) ?: periodEnd

    /**
     * 이 챌린지 자체가 [referenceToday] 기준으로 아직 끝나지 않았는지 여부.
     * 다른 챌린지와의 비교(어떤 것이 "현재 활성 챌린지"인지)에 기대지 않고
     * 이 챌린지의 기간/포기 여부만으로 판단한다. periodStart가 같은 챌린지가
     * 여러 개 있을 때(예: 포기 후 같은 날 새 챌린지 시작) 잘못된 챌린지가
     * "진행중"으로 판정되는 것을 막기 위함이다.
     */
    fun isOngoingOn(referenceToday: LocalDate): Boolean =
        abandonedDate == null &&
            !referenceToday.isBefore(periodStart) &&
            !referenceToday.isAfter(periodEnd)

    fun dDayFrom(referenceToday: LocalDate): Int =
        ChronoUnit.DAYS.between(referenceToday, periodEnd).toInt()

    fun dailyLimitOn(date: LocalDate): Int {
        var result = dailyLimit
        var bestEffectiveFrom: LocalDate? = null
        for (override in dailyLimitOverrides) {
            if (!override.effectiveFrom.isAfter(date) &&
                (bestEffectiveFrom == null || !override.effectiveFrom.isBefore(bestEffectiveFrom))
            ) {
                bestEffectiveFrom = override.effectiveFrom
                result = override.dailyLimit
            }
        }
        return result
    }
}

data class ChallengeProgress(
    val savedAmount: Int,
    val streakDays: Int,
    val dailyRecords: Map<LocalDate, DailyRecordStatus>
)

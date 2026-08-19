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
    val calendarDays: Map<LocalDate, DailyRecordStatus> = emptyMap()
) {
    val maxEditCount: Int
        get() = if (totalDays >= 15) 2 else 1

    val canEditTarget: Boolean
        get() = editCount < maxEditCount

    val effectivePeriodEnd: LocalDate
        get() = abandonedDate?.minusDays(1) ?: periodEnd

    /**
     * 이 챌린지가 더 이상 진행 중이 아닌(끝났거나 포기된) 상태인지 여부.
     * 로컬에서 방금 포기 처리를 해서 [abandonedDate]가 채워진 경우뿐 아니라, 앱 재시작 등으로
     * [abandonedDate]는 비어 있어도 서버가 이미 결과를 "FAIL"로 확정해서 내려준 경우(포기 처리도
     * 서버에서는 FAIL로 내려옴)도 포함한다. 서버 히스토리 응답(ChallengeHistoryItemDto)에는
     * abandonedDate 자체가 없어서, remoteStatus만으로 판단해야 하는 상황이 실제로 발생한다.
     */
    val isTerminal: Boolean
        get() = abandonedDate != null || remoteStatus == "FAIL"

    /**
     * 이 챌린지 자체가 [referenceToday] 기준으로 아직 끝나지 않았는지 여부.
     * 다른 챌린지와의 비교(어떤 것이 "현재 활성 챌린지"인지)에 기대지 않고
     * 이 챌린지의 기간/포기 여부만으로 판단한다. periodStart가 같은 챌린지가
     * 여러 개 있을 때(예: 포기 후 같은 날 새 챌린지 시작) 잘못된 챌린지가
     * "진행중"으로 판정되는 것을 막기 위함이다.
     */
    fun isOngoingOn(referenceToday: LocalDate): Boolean =
        !isTerminal &&
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

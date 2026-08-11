package com.example.hampouch.domain.model

import java.time.LocalDate

/**
 * 챌린지 도메인의 전체 상태.
 *
 * 예전에는 `ChallengeRepository` object가 challenges / challengeEndAcknowledged /
 * hasVisitedExpenseEditAfterEnd / noRecordDates를 각각 Compose 상태로 들고 있었다.
 * 하나의 값으로 묶어 화면은 이 값 하나만 구독하면 되게 했고, 파생 계산도 여기 모았다.
 *
 * @property noRecordDates "지출을 기록하지 않은 날"로 표시된 날짜들. 한도를 넘지 않았더라도 실패로 센다.
 */
data class ChallengeState(
    val challenges: List<ActiveChallenge> = emptyList(),
    val endAcknowledged: Boolean = false,
    val hasVisitedExpenseEditAfterEnd: Boolean = false,
    val noRecordDates: Set<LocalDate> = emptySet()
) {

    /** 가장 최근 주기의 챌린지. 목록은 periodStart 오름차순으로 유지된다. */
    val activeChallenge: ActiveChallenge? get() = challenges.lastOrNull()

    val hasOngoingChallenge: Boolean
        get() = activeChallenge?.let { !LocalDate.now().isAfter(it.effectivePeriodEnd) } ?: false

    /** 기간이 끝났는데 아직 결과를 확인하지 않은 상태. 홈에서 종료 팝업을 띄우는 조건이다. */
    fun isChallengeJustEnded(referenceToday: LocalDate): Boolean =
        activeChallenge?.let { !endAcknowledged && referenceToday.isAfter(it.periodEnd) } ?: false

    fun challengeFor(date: LocalDate): ActiveChallenge? =
        challenges.firstOrNull { !date.isBefore(it.periodStart) && !date.isAfter(it.effectivePeriodEnd) }

    fun challengeById(id: String): ActiveChallenge? = challenges.find { it.id == id }

    /** [challenge]의 시작일부터 오늘(또는 종료일 중 이른 쪽)까지의 날짜들. */
    fun elapsedDays(referenceToday: LocalDate, challenge: ActiveChallenge): List<LocalDate> {
        val trackedEnd =
            if (referenceToday.isBefore(challenge.effectivePeriodEnd)) referenceToday else challenge.effectivePeriodEnd
        return generateSequence(challenge.periodStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(trackedEnd) }
            .toList()
    }

    /**
     * 절약 금액·연속 성공일·일자별 성패를 계산한다.
     *
     * @param spentOnDate 해당 날짜의 지출 합계. 지출은 별도 도메인이라 조회 함수로 받는다.
     */
    fun computeProgress(
        referenceToday: LocalDate,
        challenge: ActiveChallenge,
        spentOnDate: (LocalDate) -> Int
    ): ChallengeProgress {
        val days = elapsedDays(referenceToday, challenge)
        fun balanceOn(date: LocalDate) = challenge.dailyLimitOn(date) - spentOnDate(date)
        fun isSuccess(date: LocalDate) = date !in noRecordDates && balanceOn(date) >= 0

        val savedAmount = days.sumOf { balanceOn(it) }
        var streakDays = 0
        for (day in days.asReversed()) {
            if (isSuccess(day)) streakDays++ else break
        }
        val dailyRecords = days.associateWith { day ->
            if (isSuccess(day)) DailyRecordStatus.SUCCESS else DailyRecordStatus.FAIL
        }
        return ChallengeProgress(savedAmount = savedAmount, streakDays = streakDays, dailyRecords = dailyRecords)
    }
}

/** 다음 챌린지 목표 금액 제안: 직전 실제 지출을 5만원 단위로 올림(최소 5만원). */
fun recommendedTightenedTarget(actualAmount: Int): Int =
    ((actualAmount / 50_000).coerceAtLeast(1)) * 50_000

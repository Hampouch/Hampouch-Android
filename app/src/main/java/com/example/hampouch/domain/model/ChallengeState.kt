package com.example.hampouch.domain.model

import java.time.LocalDate

data class ChallengeState(
    val challenges: List<ActiveChallenge> = emptyList(),
    val endAcknowledged: Boolean = false,
    val hasVisitedExpenseEditAfterEnd: Boolean = false,
    val noRecordDates: Set<LocalDate> = emptySet()
) {

    // 리스트의 마지막 요소를 그대로 쓰면 안 된다: challenges는 periodStart 기준으로 정렬되는데,
    // 포기 후 같은 날 새 챌린지를 만들면 periodStart가 같아지고, 그 상태에서 리스트에 항목이
    // 쌓인 순서(어떤 API 응답이 먼저 들어왔는지)에 따라 이미 포기/실패한 챌린지가 마지막에 남을
    // 수 있다. 그래서 끝나지 않은(isTerminal == false) 챌린지 중 가장 최근에 시작한 것을
    // "현재 챌린지"로 판단한다. isTerminal은 로컬 abandonedDate뿐 아니라 서버가 내려준
    // remoteStatus == "FAIL"도 함께 보므로, 앱 재시작 후 서버 히스토리로만 상태를 알게 된
    // 챌린지도 정상적으로 걸러진다.
    val activeChallenge: ActiveChallenge?
        get() = challenges.filterNot { it.isTerminal }.maxByOrNull { it.periodStart }

    val hasOngoingChallenge: Boolean
        get() = activeChallenge?.let { !LocalDate.now().isAfter(it.effectivePeriodEnd) } ?: false

    fun isChallengeJustEnded(referenceToday: LocalDate): Boolean =
        activeChallenge?.let { !endAcknowledged && referenceToday.isAfter(it.periodEnd) } ?: false

    fun challengeFor(date: LocalDate): ActiveChallenge? =
        challenges.firstOrNull {
            !it.isTerminal && !date.isBefore(it.periodStart) && !date.isAfter(it.effectivePeriodEnd)
        }

    fun challengeById(id: String): ActiveChallenge? = challenges.find { it.id == id }

    fun elapsedDays(referenceToday: LocalDate, challenge: ActiveChallenge): List<LocalDate> {
        val trackedEnd =
            if (referenceToday.isBefore(challenge.effectivePeriodEnd)) referenceToday else challenge.effectivePeriodEnd
        return generateSequence(challenge.periodStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(trackedEnd) }
            .toList()
    }

    fun computeProgress(
        referenceToday: LocalDate,
        challenge: ActiveChallenge,
        hasRecordOnDate: (LocalDate) -> Boolean,
        spentOnDate: (LocalDate) -> Int
    ): ChallengeProgress {
        val days = elapsedDays(referenceToday, challenge)
        fun balanceOn(date: LocalDate) = challenge.dailyLimitOn(date) - spentOnDate(date)
        fun isSuccess(date: LocalDate) = date !in noRecordDates && hasRecordOnDate(date) && balanceOn(date) >= 0

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

fun recommendedTightenedTarget(actualAmount: Int): Int =
    ((actualAmount / 50_000).coerceAtLeast(1)) * 50_000

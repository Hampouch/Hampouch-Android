package com.example.hampouch.ui.amountadjustment

import com.example.hampouch.domain.model.AmountAdjustmentChallenge
import com.example.hampouch.data.repository.ChallengeRepository
import java.time.LocalDate

object AmountAdjustmentMockData {
    /** @param spentOnDate 해당 날짜의 지출 합계. 지출 저장소는 ViewModel이 들고 있으므로 조회 함수로 받는다. */
    fun challenge(spentOnDate: (LocalDate) -> Int): AmountAdjustmentChallenge {
        val active = requireNotNull(ChallengeRepository.activeChallenge) {
            "진행중인 챌린지가 없는 상태에서 목표 금액 조정 화면에 진입했습니다."
        }
        val today = LocalDate.now()
        val trackedEnd = if (today.isBefore(active.periodEnd)) today else active.periodEnd
        val overAmount = generateSequence(active.periodStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(trackedEnd) }
            .sumOf { day ->
                (spentOnDate(day) - active.dailyLimitOn(day)).coerceAtLeast(0)
            }
        return AmountAdjustmentChallenge(
            id = active.id,
            totalDays = active.totalDays,
            dDay = active.dDayFrom(today),
            periodStart = active.periodStart,
            periodEnd = active.periodEnd,
            targetAmount = active.targetAmount,
            dailyLimit = active.dailyLimit,
            overAmount = overAmount,
            editCount = active.editCount
        )
    }
}

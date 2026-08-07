package com.example.hampouch.ui.amountadjustment

import com.example.hampouch.data.model.AmountAdjustmentChallenge
import com.example.hampouch.data.repository.ChallengeRepository
import com.example.hampouch.ui.expensedetail.ExpenseDetailStore
import java.time.LocalDate

object AmountAdjustmentMockData {
    fun challenge(): AmountAdjustmentChallenge {
        // 목표 금액 조정 화면은 진행중인 챌린지가 있을 때만(홈 화면 챌린지 카드) 진입할 수 있어 항상 존재를 가정한다.
        val active = requireNotNull(ChallengeRepository.activeChallenge) {
            "진행중인 챌린지가 없는 상태에서 목표 금액 조정 화면에 진입했습니다."
        }
        val today = LocalDate.now()
        val trackedEnd = if (today.isBefore(active.periodEnd)) today else active.periodEnd
        val overAmount = generateSequence(active.periodStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(trackedEnd) }
            .sumOf { day ->
                val spent = ExpenseDetailStore.recordsForDate(day).sumOf { it.amount }
                (spent - active.dailyLimitOn(day)).coerceAtLeast(0)
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

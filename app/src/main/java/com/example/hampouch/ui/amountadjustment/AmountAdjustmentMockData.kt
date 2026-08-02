package com.example.hampouch.ui.amountadjustment

import com.example.hampouch.data.model.AmountAdjustmentChallenge
import com.example.hampouch.data.repository.ChallengeRepository
import com.example.hampouch.ui.expensedetail.ExpenseDetailStore
import java.time.LocalDate

object AmountAdjustmentMockData {
    fun challenge(): AmountAdjustmentChallenge {
        val active = ChallengeRepository.activeChallenge
        val today = LocalDate.now()
        val trackedEnd = if (today.isBefore(active.periodEnd)) today else active.periodEnd
        val overAmount = generateSequence(active.periodStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(trackedEnd) }
            .sumOf { day ->
                val spent = ExpenseDetailStore.recordsForDate(day).sumOf { it.amount }
                (spent - active.dailyLimit).coerceAtLeast(0)
            }
        return AmountAdjustmentChallenge(
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

package com.example.hampouch.ui.home

import com.example.hampouch.domain.model.ActiveChallenge
import com.example.hampouch.domain.model.ChallengeState
import com.example.hampouch.domain.model.ExpenseEntry
import com.example.hampouch.domain.model.ExpenseRecord
import com.example.hampouch.domain.model.HomeChallenge
import com.example.hampouch.domain.model.HomeWarning
import com.example.hampouch.domain.model.HomeWarningType
import java.time.LocalDate

private const val LOW_DAILY_BUDGET_RATIO = 0.2f
private const val CATEGORY_OVERSPEND_RATIO = 0.7f
private const val REASON_OVERSPEND_RATIO = 0.7f
private const val LARGE_EXPENSE_MULTIPLIER = 2

object HomeWarningMockData {

    fun compute(
        challenge: HomeChallenge?,
        resolvedChallenge: ActiveChallenge?,
        challengeState: ChallengeState,
        referenceToday: LocalDate,
        todaysExpenses: List<ExpenseEntry>,
        recordsForDate: (LocalDate) -> List<ExpenseRecord>,
        yesterdayHasRecord: Boolean
    ): List<HomeWarning> {
        val warnings = mutableListOf<HomeWarning>()

        if (challenge != null && !challenge.isOverLimit && challenge.dailyLimit > 0) {
            val remainingRatio = challenge.todayBalance.toFloat() / challenge.dailyLimit
            if (remainingRatio in 0f..LOW_DAILY_BUDGET_RATIO) {
                warnings += HomeWarning(id = "low_daily_budget", type = HomeWarningType.LOW_DAILY_BUDGET)
            }
        }

        if (challenge != null && challenge.savedAmount < 0) {
            warnings += HomeWarning(id = "challenge_budget_exceeded", type = HomeWarningType.CHALLENGE_BUDGET_EXCEEDED)
        }

        if (resolvedChallenge != null && resolvedChallenge.targetAmount > 0) {
            val categoryTotals = mutableMapOf<String, Int>()
            challengeState.elapsedDays(referenceToday, resolvedChallenge).forEach { day ->
                recordsForDate(day).forEach { record ->
                    val categoryId = record.categoryId ?: return@forEach
                    categoryTotals[categoryId] = (categoryTotals[categoryId] ?: 0) + record.amount
                }
            }
            categoryTotals.maxByOrNull { it.value }?.let { (categoryId, total) ->
                if (total >= resolvedChallenge.targetAmount * CATEGORY_OVERSPEND_RATIO) {
                    warnings += HomeWarning(
                        id = "category_overspend_$categoryId",
                        type = HomeWarningType.CATEGORY_OVERSPEND,
                        categoryId = categoryId,
                        categorySpentAmount = total
                    )
                }
            }
        }

        val todayTotal = todaysExpenses.sumOf { it.amount }
        if (todayTotal > 0) {
            todaysExpenses
                .filter { !it.reasonTag.isNullOrBlank() }
                .groupBy { it.reasonTag!! }
                .mapValues { (_, entries) -> entries.sumOf { it.amount } }
                .maxByOrNull { it.value }
                ?.let { (reason, total) ->
                    if (total >= todayTotal * REASON_OVERSPEND_RATIO) {
                        warnings += HomeWarning(
                            id = "reason_overspend_$reason",
                            type = HomeWarningType.REASON_OVERSPEND,
                            reasonLabel = reason
                        )
                    }
                }
        }

        val isLargeSingleExpense = challenge != null && challenge.dailyLimit > 0 &&
            todayTotal >= challenge.dailyLimit * LARGE_EXPENSE_MULTIPLIER
        if (isLargeSingleExpense) {
            warnings += HomeWarning(id = "large_single_expense", type = HomeWarningType.LARGE_SINGLE_EXPENSE)
        }

        if (!yesterdayHasRecord) {
            warnings += HomeWarning(id = "missed_yesterday_record", type = HomeWarningType.MISSED_YESTERDAY_RECORD)
        }

        return warnings
    }
}

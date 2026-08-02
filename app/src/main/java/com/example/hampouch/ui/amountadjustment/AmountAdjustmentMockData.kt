package com.example.hampouch.ui.amountadjustment

import com.example.hampouch.data.model.AmountAdjustmentChallenge
import java.time.LocalDate

object AmountAdjustmentMockData {
    fun challenge(editCount: Int = 0): AmountAdjustmentChallenge = AmountAdjustmentChallenge(
        totalDays = 14,
        dDay = 7,
        periodStart = LocalDate.of(2026, 5, 1),
        periodEnd = LocalDate.of(2026, 5, 14),
        targetAmount = 140_000,
        dailyLimit = 10_000,
        overAmount = 21_400,
        editCount = editCount
    )
}

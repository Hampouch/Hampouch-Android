package com.example.hampouch.ui.home

import com.example.hampouch.R
import java.time.LocalDate

internal fun homeExpenseTitleRes(selectedDate: LocalDate, referenceToday: LocalDate): Int = when (selectedDate) {
    referenceToday -> R.string.home_today_expense_title
    referenceToday.minusDays(1) -> R.string.home_yesterday_expense_title
    else -> R.string.home_past_expense_title
}

package com.example.hampouch.ui.home

import com.example.hampouch.R
import com.example.hampouch.domain.model.ExpenseEntry
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeExpenseListTest {
    @Test
    fun `선택 날짜에 따라 홈 지출 제목을 구분한다`() {
        val today = LocalDate.of(2026, 8, 19)

        assertEquals(R.string.home_today_expense_title, homeExpenseTitleRes(today, today))
        assertEquals(R.string.home_yesterday_expense_title, homeExpenseTitleRes(today.minusDays(1), today))
        assertEquals(R.string.home_past_expense_title, homeExpenseTitleRes(today.minusDays(2), today))
    }

    @Test
    fun `홈 화면에는 최신 지출 네 개만 표시한다`() {
        val expenses = (5 downTo 1).map { id ->
            ExpenseEntry(id = id.toString(), amount = id * 1_000)
        }

        assertEquals(listOf("5", "4", "3", "2"), recentHomeExpenses(expenses).map { it.id })
    }
}

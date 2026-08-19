package com.example.hampouch.ui.home

import com.example.hampouch.domain.model.ExpenseEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeExpenseListTest {
    @Test
    fun `홈 화면에는 최신 지출 네 개만 표시한다`() {
        val expenses = (5 downTo 1).map { id ->
            ExpenseEntry(id = id.toString(), amount = id * 1_000)
        }

        assertEquals(listOf("5", "4", "3", "2"), recentHomeExpenses(expenses).map { it.id })
    }
}

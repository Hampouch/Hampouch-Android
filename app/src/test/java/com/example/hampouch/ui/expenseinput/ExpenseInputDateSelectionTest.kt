package com.example.hampouch.ui.expenseinput

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class ExpenseInputDateSelectionTest {

    private val today = LocalDate.of(2026, 8, 13)

    @Test
    fun `today and past dates are selectable`() {
        assertTrue(isExpenseInputDateSelectable(today.toUtcMillis(), today))
        assertTrue(isExpenseInputDateSelectable(today.minusDays(1).toUtcMillis(), today))
    }

    @Test
    fun `future dates are not selectable`() {
        assertFalse(isExpenseInputDateSelectable(today.plusDays(1).toUtcMillis(), today))
    }

    private fun LocalDate.toUtcMillis(): Long =
        atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

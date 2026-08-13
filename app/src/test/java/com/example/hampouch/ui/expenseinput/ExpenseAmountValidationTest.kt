package com.example.hampouch.ui.expenseinput

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseAmountValidationTest {

    @Test
    fun `existing maximum amount is valid`() {
        assertTrue(isExpenseAmountValid(MaxExpenseAmount))
    }

    @Test
    fun `amount above existing maximum is invalid`() {
        assertFalse(isExpenseAmountValid(MaxExpenseAmount + 1))
    }
}

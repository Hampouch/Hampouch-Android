package com.example.hampouch.data.repository

import com.example.hampouch.domain.model.ApiException
import org.junit.Assert.assertEquals
import org.junit.Test

class ExpenseDayResponseValidationTest {

    @Test
    fun `Swagger non-null field is returned when present`() {
        assertEquals("스타벅스", requireExpenseDayString("스타벅스", "name"))
    }

    @Test
    fun `invalid response keeps the existing network error message`() {
        val error: ApiException = expenseDayResponseError()

        assertEquals("NETWORK_ERROR", error.code)
        assertEquals("인터넷 연결을 확인해주세요.", error.message)
    }
}

package com.example.hampouch.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class ExpenseCategoryRequestTest {
    @Test
    fun `기타 카테고리 칩은 ETC와 기타로 변환한다`() {
        assertEquals("ETC" to "기타", expenseCategoryRequestFields("etc", null))
    }

    @Test
    fun `직접 입력 카테고리는 ETC와 입력값으로 변환한다`() {
        assertEquals("ETC" to "야식", expenseCategoryRequestFields(null, "야식"))
    }
}

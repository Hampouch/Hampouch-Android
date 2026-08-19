package com.example.hampouch.ui.onboarding

import com.example.hampouch.ui.onboarding.steps.directPeriodInputValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PeriodStepTest {
    private val presets = setOf(7, 14, 30)

    @Test
    fun `프리셋 기간은 직접 입력칸에 표시하지 않는다`() {
        assertNull(directPeriodInputValue(7, presets))
        assertNull(directPeriodInputValue(14, presets))
        assertNull(directPeriodInputValue(30, presets))
    }

    @Test
    fun `프리셋이 아닌 기간은 직접 입력칸에 표시한다`() {
        assertEquals(45, directPeriodInputValue(45, presets))
    }
}

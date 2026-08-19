package com.example.hampouch.ui.onboarding

import com.example.hampouch.ui.onboarding.components.exceedsAmountMax
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingAmountInputTest {
    @Test
    fun `빈 입력은 최댓값 초과로 처리하지 않는다`() {
        assertFalse(exceedsAmountMax("", 999_999_999))
    }

    @Test
    fun `설정된 최댓값을 넘는 숫자만 초과로 처리한다`() {
        assertFalse(exceedsAmountMax("999999999", 999_999_999))
        assertTrue(exceedsAmountMax("1000000000", 999_999_999))
        assertTrue(exceedsAmountMax("999999999999999999999", 999_999_999))
    }
}

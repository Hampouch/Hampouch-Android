package com.example.hampouch.ui.nextchallenge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NextChallengePeriodTest {
    @Test
    fun `직접 입력 완료 시 프리셋과 일치하는 기간을 반환한다`() {
        assertEquals(7, matchingPeriodPreset(7))
        assertEquals(14, matchingPeriodPreset(14))
        assertEquals(30, matchingPeriodPreset(30))
    }

    @Test
    fun `직접 입력 완료 시 프리셋이 아닌 기간은 반환하지 않는다`() {
        assertNull(matchingPeriodPreset(45))
        assertNull(matchingPeriodPreset(null))
    }
}

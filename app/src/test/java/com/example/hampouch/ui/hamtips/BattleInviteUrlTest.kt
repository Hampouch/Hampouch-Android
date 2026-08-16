package com.example.hampouch.ui.hamtips

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BattleInviteUrlTest {

    @Test
    fun `extracts battle code from invitation url`() {
        assertEquals(
            "ABC123",
            extractBattleCode("https://hampouch.app/battles/invite/ABC123")
        )
    }

    @Test
    fun `ignores query fragment and trailing slash`() {
        assertEquals(
            "ABC123",
            extractBattleCode("https://hampouch.app/battles/invite/ABC123/?source=community#join")
        )
    }

    @Test
    fun `accepts raw battle code for backward compatibility`() {
        assertEquals("ABC123", extractBattleCode("ABC123"))
    }

    @Test
    fun `rejects empty invitation code`() {
        assertNull(extractBattleCode("https://hampouch.app/battles/invite/"))
    }
}

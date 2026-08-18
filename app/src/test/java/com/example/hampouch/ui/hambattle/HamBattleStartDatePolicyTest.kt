package com.example.hampouch.ui.hambattle

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class HamBattleStartDatePolicyTest {
    private val referenceToday = LocalDate.of(2026, 8, 18)

    @Test
    fun `오늘까지는 햄배틀 시작일로 선택할 수 없다`() {
        assertFalse(isBattleStartDateSelectable(referenceToday.minusDays(1).toUtcMillis(), referenceToday))
        assertFalse(isBattleStartDateSelectable(referenceToday.toUtcMillis(), referenceToday))
    }

    @Test
    fun `내일부터는 햄배틀 시작일로 선택할 수 있다`() {
        assertTrue(isBattleStartDateSelectable(referenceToday.plusDays(1).toUtcMillis(), referenceToday))
        assertTrue(isBattleStartDateSelectable(referenceToday.plusDays(30).toUtcMillis(), referenceToday))
    }

    private fun LocalDate.toUtcMillis(): Long =
        atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

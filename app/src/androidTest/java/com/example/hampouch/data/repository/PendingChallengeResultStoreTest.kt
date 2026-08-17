package com.example.hampouch.data.repository

import androidx.test.platform.app.InstrumentationRegistry
import com.example.hampouch.domain.model.DailyRecordStatus
import com.example.hampouch.domain.model.EmotionStat
import com.example.hampouch.domain.model.PendingChallengeResult
import com.example.hampouch.domain.model.SpendingEmotion
import java.time.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PendingChallengeResultStoreTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var store: PendingChallengeResultStore

    @Before
    fun setUp() {
        store = PendingChallengeResultStore(context)
        store.clear()
    }

    @After
    fun tearDown() {
        store.clear()
    }

    @Test
    fun pendingChallengeResult_survivesStoreRecreationUntilCleared() {
        val startDate = LocalDate.of(2026, 8, 1)
        val pending = PendingChallengeResult(
            userId = 7L,
            challengeId = "42",
            title = "14일 챌린지",
            periodStart = startDate,
            periodEnd = LocalDate.of(2026, 8, 14),
            totalDays = 14,
            successDays = 5,
            streakDays = 3,
            amountValue = 20_000,
            goalAmount = 300_000,
            actualAmount = 320_000,
            dailyLimit = 21_428,
            emotionStats = listOf(EmotionStat(SpendingEmotion.STRESS, percent = 60, amount = 192_000)),
            dailyRecords = mapOf(startDate to DailyRecordStatus.SUCCESS)
        )

        store.save(pending)
        val recreatedStore = PendingChallengeResultStore(context)

        assertEquals(pending, recreatedStore.pending.value)
        recreatedStore.clear()
        assertNull(PendingChallengeResultStore(context).pending.value)
    }
}

package com.example.hampouch.ui.mypage

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.example.hampouch.R
import com.example.hampouch.domain.model.ChallengeRecord
import com.example.hampouch.domain.model.ChallengeStatus
import com.example.hampouch.ui.theme.HampouchTheme
import org.junit.Rule
import org.junit.Test

class ChallengeHistoryScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyRecords_showsEmptyMessage() {
        val emptyMessage = targetContext().getString(R.string.challenge_history_empty_message)

        setContent(records = emptyList())

        composeRule.onNodeWithText(emptyMessage).assertIsDisplayed()
    }

    @Test
    fun singleRecord_showsRecord() {
        val record = challengeRecord(index = 1)
        val title = targetContext().getString(R.string.challenge_history_card_title_format, record.totalDays)

        setContent(records = listOf(record))

        composeRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun longRecords_scrollsToLastRecord() {
        val records = List(30, ::challengeRecord)
        val lastRecord = records.last()
        val lastTitle = targetContext().getString(
            R.string.challenge_history_card_title_format,
            lastRecord.totalDays
        )

        setContent(records = records)

        composeRule.onNodeWithText(lastTitle).performScrollTo().assertIsDisplayed()
    }

    private fun setContent(records: List<ChallengeRecord>) {
        composeRule.setContent {
            HampouchTheme {
                ChallengeHistoryScreen(
                    records = records,
                    onBackClick = {},
                    onRecordClick = {}
                )
            }
        }
    }

    private fun challengeRecord(index: Int) = ChallengeRecord(
        id = "record-$index",
        status = ChallengeStatus.SUCCESS,
        totalDays = index + 1,
        startDateLabel = "2026.07.01",
        endDateLabel = "2026.07.31",
        targetAmount = 300_000,
        actualAmount = 250_000
    )

    private fun targetContext() = InstrumentationRegistry.getInstrumentation().targetContext
}

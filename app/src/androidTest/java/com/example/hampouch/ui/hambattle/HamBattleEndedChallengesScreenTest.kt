package com.example.hampouch.ui.hambattle

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.example.hampouch.data.local.HamBattleMockFixtures
import com.example.hampouch.domain.model.HamBattleChallenge
import com.example.hampouch.ui.theme.HampouchTheme
import org.junit.Rule
import org.junit.Test

class HamBattleEndedChallengesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyChallenges_showsSectionHeader() {
        setContent(challenges = emptyList())

        composeRule.onNodeWithText("종료").assertIsDisplayed()
    }

    @Test
    fun singleChallenge_showsChallenge() {
        val challenge = challenge(index = 1)

        setContent(challenges = listOf(challenge))

        composeRule.onNodeWithText(challenge.title).assertIsDisplayed()
    }

    @Test
    fun longChallenges_scrollsToLastChallenge() {
        val challenges = List(30, ::challenge)
        val lastChallenge = challenges.last()

        setContent(challenges = challenges)

        composeRule.onNodeWithText(lastChallenge.title).performScrollTo().assertIsDisplayed()
    }

    private fun setContent(challenges: List<HamBattleChallenge>) {
        composeRule.setContent {
            HampouchTheme {
                HamBattleEndedChallengesScreen(
                    endedChallenges = challenges,
                    onBackClick = {},
                    onChallengeClick = {}
                )
            }
        }
    }

    private fun challenge(index: Int) = HamBattleMockFixtures.endedChallenges().first().copy(
        id = "ended-$index",
        title = "종료 챌린지 $index"
    )
}

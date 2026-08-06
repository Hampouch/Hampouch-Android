package com.example.hampouch.ui.hambattle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.data.model.HamBattleActiveChallenge
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HampouchTheme

private const val PAGE_PODIUM = 0
private const val PAGE_RANKING_LIST = 1
private const val PAGE_COUNT = 2

@Composable
fun HamBattleChallengesResultPagerScreen(
    challenge: HamBattleActiveChallenge,
    onBackClick: () -> Unit = {},
    onStartNewChallengeClick: () -> Unit = {}
) {
    val pagerState = rememberPagerState(initialPage = PAGE_PODIUM) { PAGE_COUNT }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            when (page) {
                PAGE_PODIUM -> HamBattleChallengesPodiumResultScreen(
                    challenge = challenge,
                    onBackClick = onBackClick,
                    onStartNewChallengeClick = onStartNewChallengeClick
                )

                else -> HamBattleChallengesResultScreen(
                    challenge = challenge,
                    onBackClick = onBackClick,
                    onStartNewChallengeClick = onStartNewChallengeClick
                )
            }
        }

        ResultPageIndicator(
            pageCount = PAGE_COUNT,
            currentPage = pagerState.currentPage,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}

@Composable
private fun ResultPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            Box(
                modifier = Modifier
                    .size(if (selected) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (selected) HPMain else HPGray4)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultPageIndicatorPreview() {
    HampouchTheme {
        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
            ResultPageIndicator(pageCount = 3, currentPage = 1)
        }
    }
}

@Preview
@Composable
private fun HamBattleChallengesResultPagerScreenPreview() {
    HampouchTheme {
        HamBattleChallengesResultPagerScreen(
            challenge = HamBattleMockData.activeChallenges.first()
        )
    }
}

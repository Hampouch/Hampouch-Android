package com.example.hampouch.ui.hambattle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
fun HamBattleChallengeResultPagerScreen(
    challenge: HamBattleActiveChallenge,
    onBackClick: () -> Unit = {},
    onStartNewChallengeClick: () -> Unit = {}
) {
    val pagerState = rememberPagerState(initialPage = PAGE_PODIUM) { PAGE_COUNT }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                PAGE_PODIUM -> HamBattleChallengeResultScreen(
                    challenge = challenge,
                    onBackClick = onBackClick,
                    onStartNewChallengeClick = onStartNewChallengeClick
                )

                else -> HamBattleOneVsOneResultScreen(
                    challenge = challenge,
                    onBackClick = onBackClick,
                    onStartNewChallengeClick = onStartNewChallengeClick
                )
            }
        }

        // 스크롤과 무관하게 화면(하단 네비바 바로 위) 고정 위치에 표시.
        ResultPageIndicator(
            pageCount = PAGE_COUNT,
            currentPage = pagerState.currentPage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
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

@Preview
@Composable
private fun HamBattleChallengeResultPagerScreenPreview() {
    HampouchTheme {
        HamBattleChallengeResultPagerScreen(
            challenge = HamBattleMockData.activeChallenges.first()
        )
    }
}

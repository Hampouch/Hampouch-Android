package com.example.hampouch.ui.widget

import androidx.compose.runtime.Composable
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import com.example.hampouch.domain.model.HomeChallenge

private val previewChubbyChallenge = HomeChallenge(
    totalDays = 14,
    dDay = 10,
    periodStartLabel = "5월 1일",
    periodEndLabel = "5월 14일",
    dailyLimit = 20_000,
    todayBalance = 17_300,
    savedAmount = 21_400,
    streakDays = 4
)
private val previewNormalChallenge = previewChubbyChallenge.copy(todayBalance = 7_300)
private val previewThinChallenge = previewChubbyChallenge.copy(todayBalance = 300)


@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 250, heightDp = 110)
@Composable
private fun HomeWidgetChubbyPreview() {
    HomeWidgetContent(HomeWidgetState.InProgress(previewChubbyChallenge))
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 250, heightDp = 110)
@Composable
private fun HomeWidgetNormalPreview() {
    HomeWidgetContent(HomeWidgetState.InProgress(previewNormalChallenge))
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 250, heightDp = 110)
@Composable
private fun HomeWidgetThinPreview() {
    HomeWidgetContent(HomeWidgetState.InProgress(previewThinChallenge))
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 250, heightDp = 110)
@Composable
private fun HomeWidgetNoActiveChallengePreview() {
    HomeWidgetContent(HomeWidgetState.NoActiveChallenge)
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 250, heightDp = 110)
@Composable
private fun HomeWidgetLoggedOutPreview() {
    HomeWidgetContent(HomeWidgetState.LoggedOut)
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 250, heightDp = 110)
@Composable
private fun HomeWidgetLoadingPreview() {
    HomeWidgetContent(HomeWidgetState.Loading)
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 250, heightDp = 110)
@Composable
private fun HomeWidgetRestingPreview() {
    HomeWidgetContent(HomeWidgetState.Resting(plannedResumeDateLabel = "8월 20일"))
}


@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 522, heightDp = 230)
@Composable
private fun HomeWidgetChubbyMaxSizePreview() {
    HomeWidgetContent(HomeWidgetState.InProgress(previewChubbyChallenge))
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 522, heightDp = 230)
@Composable
private fun HomeWidgetNoActiveChallengeMaxSizePreview() {
    HomeWidgetContent(HomeWidgetState.NoActiveChallenge)
}

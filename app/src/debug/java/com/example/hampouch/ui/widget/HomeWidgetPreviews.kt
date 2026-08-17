package com.example.hampouch.ui.widget

import androidx.compose.runtime.Composable
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import com.example.hampouch.domain.model.HomeChallenge

/**
 * Android Studio에서 위젯을 그려보기 위한 @Preview 모음. debug 빌드에만 포함된다
 * (androidx.glance:glance-preview / glance-appwidget-preview가 debugImplementation이라서).
 *
 * [HomeWidgetContent]는 실제 [HomeWidget.provideGlance]가 그리는 것과 동일한 컴포저블이라,
 * 여기 프리뷰가 곧 실제 위젯 렌더링과 같다. 데이터는 [com.example.hampouch.data.repository.ChallengeRepository]의
 * 목데이터(한도 20,000원 · 연속 달성 4일)와 같은 값을 써서 홈 화면 프리뷰들과 숫자를 맞췄다.
 *
 * 실제 런처의 4x2 최소 크기(250x110)와 Figma 원본 크기(522x230)를 함께 확인한다.
 */
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

// --- 실제 런처 최소 크기 (250x110, 4x2) ---

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

// --- Figma 원본 기준 크기 (522x230) ---

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

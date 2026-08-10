package com.example.hampouch.ui.widget

import androidx.compose.runtime.Composable
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import com.example.hampouch.data.model.HomeChallenge

/**
 * Android Studio에서 위젯을 그려보기 위한 @Preview 모음. debug 빌드에만 포함된다
 * (androidx.glance:glance-preview / glance-appwidget-preview가 debugImplementation이라서).
 *
 * [HomeWidgetContent]는 실제 [HomeWidget.provideGlance]가 그리는 것과 동일한 컴포저블이라,
 * 여기 프리뷰가 곧 실제 위젯 렌더링과 같다. 데이터는 [com.example.hampouch.data.repository.ChallengeRepository]의
 * 목데이터(한도 20,000원 · 연속 달성 4일)와 같은 값을 써서 홈 화면 프리뷰들과 숫자를 맞췄다.
 *
 * 크기는 두 세트로 나눠서 본다:
 * - 250x110 (`home_widget_info.xml`의 minWidth/minHeight): 대부분의 폰에서 실제로 보이는 기본 크기.
 * - 522x230 (`maxResizeWidth`/`maxResizeHeight`, 피그마 원본 프레임과 같은 크기): 사용자가 위젯을
 *   최대로 늘렸을 때. [HomeWidget.sizeMode]가 [androidx.glance.appwidget.SizeMode.Exact]라
 *   두 크기 모두 [rememberWidgetScale]로 자동으로 배율이 맞춰진다 — 두 프리뷰가 서로 다른 코드 경로가
 *   아니라 같은 코드가 크기만 다르게 받는 것이라, 여기서 안 깨지면 그 사이 크기들도 안 깨진다.
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

// --- 기본 크기 (250x110) ---

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

// --- 최대 리사이즈 크기 (522x230, 피그마 원본과 동일) ---

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

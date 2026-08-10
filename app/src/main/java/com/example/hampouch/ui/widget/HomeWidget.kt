package com.example.hampouch.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.unit.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import com.example.hampouch.MainActivity
import com.example.hampouch.R
import com.example.hampouch.data.model.CharacterState
import com.example.hampouch.data.model.HomeChallenge
import com.example.hampouch.ui.home.components.characterDrawableRes
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray3
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite

/**
 * 홈 화면 "식비 절약 챌린지" 위젯의 [GlanceAppWidget].
 * 피그마 "위젯" 섹션(node-id 2627:19676)의 4가지 카드(여유/주의/부족/휴식기)를 그대로 옮긴 구성이다.
 * 텍스트는 Pretendard 적용을 위해 [PretendardText](AndroidRemoteViews 기반)를 쓴다.
 *
 * 위젯은 런처/기기마다 실제로 배정되는 크기가 다르고 사용자가 리사이즈도 할 수 있어서
 * (`home_widget_info.xml`의 resizeMode) 피그마의 522x230을 그대로 박아넣을 수 없다.
 * 그래서 [SizeMode.Exact]로 매번 시스템이 실제로 준 크기(가로/세로 각각)를 [LocalSize]로 받고,
 * [rememberWidgetScale]에서 가로·세로 배율을 따로 계산해 [WidgetScale]로 넘긴다 — 가로 방향 값(패딩,
 * 마스코트 너비 등)은 가로 배율로, 세로 방향 값(줄 높이, 세로 여백)은 세로 배율로 각각 스케일한다.
 */
class HomeWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = HomeWidgetDataProvider.currentState()
        provideContent {
            HomeWidgetContent(state)
        }
    }
}

/** [HomeWidget]을 홈 화면에 등록하는 리시버. AndroidManifest.xml의 receiver 선언과 짝이다. */
class HomeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HomeWidget()
}

/** 이 값 기준(=home_widget_info.xml의 minWidth/minHeight)으로 잡아둔 여백/크기 비율이 배율 1.0이 된다. */
private val BaseWidgetWidth = 250.dp
private val BaseWidgetHeight = 110.dp
private const val MinWidgetScale = 0.85f
private const val MaxWidgetScale = 2.3f

/**
 * @param width 가로 방향 값(좌우 패딩, 마스코트 너비, 가로 간격 등)에 곱하는 배율.
 * @param height 세로 방향 값(상하 패딩, 줄 높이, 세로 간격 등)에 곱하는 배율.
 */
private data class WidgetScale(val width: Float, val height: Float) {
    /** 글자 크기·모서리 반경처럼 가로/세로 어느 쪽으로도 넘치면 안 되는 값에 쓰는, 더 작게 늘어난 쪽 배율. */
    val text: Float get() = minOf(width, height)
}

/** 지금 실제로 그려지는 위젯 크기([LocalSize])를 기준 크기와 비교해 가로/세로 배율을 각각 구한다. */
@Composable
private fun rememberWidgetScale(): WidgetScale {
    val size = LocalSize.current
    val widthScale = (size.width / BaseWidgetWidth).coerceIn(MinWidgetScale, MaxWidgetScale)
    val heightScale = (size.height / BaseWidgetHeight).coerceIn(MinWidgetScale, MaxWidgetScale)
    return WidgetScale(widthScale, heightScale)
}

private fun Dp.scaled(scale: Float): Dp = (value * scale).dp
private fun TextUnit.scaled(scale: Float): TextUnit = (value * scale).sp

@Composable
internal fun HomeWidgetContent(state: HomeWidgetState) {
    val scale = rememberWidgetScale()
    // 패딩은 각 상태 컴포저블 안에서 필요한 만큼만 준다(NoActiveChallengeContent의 마스코트처럼
    // 카드 가장자리에 붙어야 하는 요소가 있어서, 여기서 일괄로 주면 안 된다).
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.bg_widget_card))
            .clickable(actionStartActivity<MainActivity>())
    ) {
        when (state) {
            is HomeWidgetState.InProgress -> ChallengeProgressContent(state.challenge, scale)
            HomeWidgetState.NoActiveChallenge -> NoActiveChallengeContent(scale)
        }
    }
}

@Composable
private fun ChallengeProgressContent(challenge: HomeChallenge, scale: WidgetScale) {
    val context = LocalContext.current
    val gaugeColor = when (challenge.characterState) {
        CharacterState.CHUBBY -> HPSub2
        CharacterState.NORMAL -> HPMain
        CharacterState.THIN, CharacterState.OVER_LIMIT -> HPSub
    }

    // 피그마(522x230): 좌우 패딩 20px(3.83%) · 상하 패딩 25px(10.87%) — 카드 크기에 비례해서 다시 계산.
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(horizontal = 10.dp.scaled(scale.width), vertical = 12.dp.scaled(scale.height)),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        // 피그마에서 마스코트 높이는 정보 영역 높이의 100%(=180/180)라, 고정 dp 대신 fillMaxHeight로
        // "이 행에서 실제로 쓸 수 있는 세로 공간을 그대로 채운다"를 그대로 옮겼다. 너비만 배율을 준다.
        Image(
            provider = ImageProvider(characterDrawableRes(challenge.characterState)),
            contentDescription = context.getString(R.string.cd_hamster_character),
            modifier = GlanceModifier.width(76.dp.scaled(scale.width)).fillMaxHeight()
        )
        Spacer(modifier = GlanceModifier.width(4.dp.scaled(scale.width)))
        Column(modifier = GlanceModifier.fillMaxHeight().defaultWeight()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    PretendardText(
                        text = context.getString(
                            R.string.home_challenge_in_progress_format,
                            challenge.totalDays
                        ),
                        color = HPBlack,
                        fontSize = 15.sp.scaled(scale.text),
                        weight = PretendardWeight.SemiBold,
                        maxLines = 1,
                        modifier = GlanceModifier.fillMaxWidth().height(22.dp.scaled(scale.text))
                    )
                    PretendardText(
                        text = context.getString(
                            R.string.home_challenge_period_format,
                            challenge.periodStartLabel,
                            challenge.periodEndLabel
                        ),
                        color = HPText,
                        fontSize = 12.sp.scaled(scale.text),
                        weight = PretendardWeight.Medium,
                        maxLines = 1,
                        modifier = GlanceModifier.fillMaxWidth().height(18.dp.scaled(scale.text))
                    )
                }
                StreakBadge(streakDays = challenge.streakDays, scale = scale)
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // 피그마 순서: 오늘 잔액(라벨+금액) → 게이지 → 한도 텍스트.
            Row(
                modifier = GlanceModifier.fillMaxWidth().height(28.dp.scaled(scale.text)),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                PretendardText(
                    text = context.getString(R.string.home_today_balance_label),
                    color = HPBlack,
                    fontSize = 12.sp.scaled(scale.text),
                    weight = PretendardWeight.Medium,
                    maxLines = 1,
                    modifier = GlanceModifier.width(56.dp.scaled(scale.text)).height(18.dp.scaled(scale.text))
                )
                PretendardText(
                    text = context.getString(R.string.home_amount_won_format, formatWon(challenge.todayBalance)),
                    color = if (challenge.isOverLimit) HPSub else HPBlack,
                    fontSize = 20.sp.scaled(scale.text),
                    weight = PretendardWeight.Bold,
                    textAlign = WidgetTextAlign.End,
                    maxLines = 1,
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight()
                )
            }
            Spacer(modifier = GlanceModifier.height(4.dp.scaled(scale.height)))
            LinearProgressIndicator(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(10.dp.scaled(scale.height))
                    .cornerRadius(5.dp.scaled(scale.height)),
                progress = challenge.balanceRatio,
                color = ColorProvider(gaugeColor),
                backgroundColor = ColorProvider(HPGray3)
            )
            Spacer(modifier = GlanceModifier.height(4.dp.scaled(scale.height)))
            PretendardText(
                text = context.getString(R.string.home_limit_label_format, formatWon(challenge.dailyLimit)),
                color = HPText,
                fontSize = 11.sp.scaled(scale.text),
                weight = PretendardWeight.Medium,
                textAlign = WidgetTextAlign.End,
                maxLines = 1,
                modifier = GlanceModifier.fillMaxWidth().height(16.dp.scaled(scale.text))
            )
        }
    }
}

@Composable
private fun StreakBadge(streakDays: Int, scale: WidgetScale, modifier: GlanceModifier = GlanceModifier) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .background(ColorProvider(HPMain))
            .cornerRadius(10.dp.scaled(scale.text))
            .padding(horizontal = 10.dp.scaled(scale.text), vertical = 6.dp.scaled(scale.text))
    ) {
        PretendardText(
            text = context.getString(R.string.home_streak_label) + " " +
                context.getString(R.string.home_streak_days_format, streakDays),
            color = HPWhite,
            fontSize = 13.sp.scaled(scale.text),
            weight = PretendardWeight.SemiBold,
            textAlign = WidgetTextAlign.Center,
            maxLines = 1,
            modifier = GlanceModifier.width(96.dp.scaled(scale.text)).height(26.dp.scaled(scale.text))
        )
    }
}

/**
 * 휴식기(진행 중인 챌린지 없음) 화면.
 *
 * 피그마 원본은 마스코트를 텍스트 위에 절대좌표로 겹쳐 그리고, 딱 522x230에서만 살짝 안 겹치게
 * 손으로 맞춘 배치라 크기가 조금만 달라져도(리사이즈, 다른 화면 크기) 텍스트와 마스코트가
 * 겹치거나 잘렸다. 그래서 겹칠 수 없는 구조(Row로 텍스트 칸과 마스코트 칸을 아예 분리)로 바꿨다 —
 * 어떤 크기에서도 항상 텍스트 칸(defaultWeight)과 마스코트 칸이 서로 침범하지 않는다.
 */
@Composable
private fun NoActiveChallengeContent(scale: WidgetScale) {
    val context = LocalContext.current
    // 피그마 좌우 패딩(30px)을 그대로 스케일하면 마스코트까지 더해서 텍스트 칸이 너무 좁아져
    // "포치와 함께 식비를 절약해봐요"의 끝(요)이 잘리고 "절약"이 "절"/"약"으로 쪼개져 줄바꿈됐다.
    // 문구가 길어서(피그마보다 실제 폰트 크기를 키워 쓰는 만큼) 패딩은 진행중 상태와 같게 줄였다.
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(horizontal = 10.dp.scaled(scale.width), vertical = 12.dp.scaled(scale.height)),
        verticalAlignment = Alignment.Vertical.Bottom
    ) {
        Column(
            modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            PretendardText(
                text = context.getString(R.string.home_no_challenge_title),
                color = HPBlack,
                fontSize = 15.sp.scaled(scale.text),
                weight = PretendardWeight.SemiBold,
                maxLines = 2,
                modifier = GlanceModifier.fillMaxWidth().height(48.dp.scaled(scale.text))
            )
            Spacer(modifier = GlanceModifier.height(4.dp.scaled(scale.height)))
            PretendardText(
                text = context.getString(R.string.home_no_challenge_subtitle),
                color = HPText,
                fontSize = 12.sp.scaled(scale.text),
                weight = PretendardWeight.Medium,
                maxLines = 1,
                modifier = GlanceModifier.fillMaxWidth().height(18.dp.scaled(scale.text))
            )
            Spacer(modifier = GlanceModifier.height(10.dp.scaled(scale.height)))
            PretendardText(
                text = context.getString(R.string.home_no_challenge_cta),
                color = HPSub1,
                fontSize = 12.sp.scaled(scale.text),
                weight = PretendardWeight.SemiBold,
                maxLines = 1,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(22.dp.scaled(scale.text))
                    .clickable(actionStartActivity<MainActivity>())
            )
        }
        Spacer(modifier = GlanceModifier.width(4.dp.scaled(scale.width)))
        // 피그마 원본 비율(마스코트 160x139, 카드 522x230)을 유지하되, 문구가 길어서 텍스트 칸에
        // 폭을 더 양보하도록 진행중 상태(76dp)보다 작게 잡았다(비율 160:139 ≈ 1.15는 유지).
        Image(
            provider = ImageProvider(R.drawable.img_widget_hamster_normal),
            contentDescription = context.getString(R.string.cd_hamster_character),
            modifier = GlanceModifier.size(
                width = 60.dp.scaled(scale.width),
                height = 52.dp.scaled(scale.height)
            )
        )
    }
}

private fun formatWon(amount: Int): String = "%,d".format(amount)

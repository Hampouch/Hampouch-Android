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
import com.example.hampouch.domain.model.HomeChallenge
import com.example.hampouch.ui.home.components.characterDrawableRes
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite

/**
 * 홈 화면 "식비 절약 챌린지" 위젯의 [GlanceAppWidget].
 * 앱 홈 화면의 챌린지 카드 구조와 수치를 Glance로 옮긴 구성이다.
 * 텍스트는 Pretendard 적용을 위해 [PretendardText](AndroidRemoteViews 기반)를 쓴다.
 *
 * 위젯은 런처/기기마다 실제로 배정되는 크기가 다르고 사용자가 리사이즈도 할 수 있어서
 * [SizeMode.Exact]와 [LocalSize]로 실제 할당 크기를 읽고 홈 카드 비율을 유지하는 단일 배율을 적용한다.
 */
class HomeWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = HomeWidgetSnapshotStore(context).read()
        provideContent {
            HomeWidgetContent(state)
        }
    }
}

/** [HomeWidget]을 홈 화면에 등록하는 리시버. AndroidManifest.xml의 receiver 선언과 짝이다. */
class HomeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HomeWidget()
}

/** 일반적인 앱 홈 콘텐츠 폭과 전체 챌린지 카드 높이에서 배율 1.0이 된다. */
private val DESIGN_WIDGET_WIDTH = 353.dp
private val DESIGN_WIDGET_HEIGHT = 468.dp
private val COMPACT_WIDGET_HEIGHT = 220.dp
private val FULL_CARD_HEIGHT_THRESHOLD = 360.dp
private const val MIN_LAYOUT_SCALE = 0.7f
private const val MAX_LAYOUT_SCALE = 1.15f

/**
 * @param width 가로 방향 값(좌우 패딩, 마스코트 너비, 가로 간격 등)에 곱하는 배율.
 * @param height 세로 방향 값(상하 패딩, 줄 높이, 세로 간격 등)에 곱하는 배율.
 */
private data class WidgetScale(val width: Float, val height: Float) {
    val text: Float get() = minOf(width, height)
}

/** 지금 실제로 그려지는 위젯 크기([LocalSize])를 기준 크기와 비교해 가로/세로 배율을 각각 구한다. */
@Composable
private fun rememberWidgetScale(compact: Boolean): WidgetScale {
    val size = LocalSize.current
    val designHeight = if (compact) COMPACT_WIDGET_HEIGHT else DESIGN_WIDGET_HEIGHT
    val uniformScale = minOf(size.width / DESIGN_WIDGET_WIDTH, size.height / designHeight)
        .coerceIn(MIN_LAYOUT_SCALE, MAX_LAYOUT_SCALE)
    return WidgetScale(uniformScale, uniformScale)
}

private fun Dp.scaled(scale: Float): Dp = (value * scale).dp
private fun TextUnit.scaled(scale: Float): TextUnit = (value * scale).sp

@Composable
internal fun HomeWidgetContent(state: HomeWidgetState) {
    val compact = LocalSize.current.height < FULL_CARD_HEIGHT_THRESHOLD
    val scale = rememberWidgetScale(compact)
    // 패딩은 각 상태 컴포저블 안에서 필요한 만큼만 준다(NoActiveChallengeContent의 마스코트처럼
    // 카드 가장자리에 붙어야 하는 요소가 있어서, 여기서 일괄로 주면 안 된다).
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivity<MainActivity>())
    ) {
        when (state) {
            is HomeWidgetState.InProgress -> if (compact) {
                CompactChallengeContent(state.challenge, scale)
            } else {
                ChallengeProgressContent(state.challenge, scale)
            }
            HomeWidgetState.LoggedOut -> IdleContent(
                titleRes = R.string.widget_login_title,
                subtitle = LocalContext.current.getString(R.string.widget_login_subtitle),
                ctaRes = R.string.widget_login_cta,
                scale = scale
            )
            HomeWidgetState.Loading -> IdleContent(
                titleRes = R.string.widget_loading_title,
                subtitle = LocalContext.current.getString(R.string.widget_loading_subtitle),
                ctaRes = R.string.widget_open_app_cta,
                scale = scale
            )
            is HomeWidgetState.Resting -> IdleContent(
                titleRes = R.string.widget_resting_title,
                subtitle = LocalContext.current.getString(
                    R.string.widget_resting_subtitle_format,
                    state.plannedResumeDateLabel
                ),
                ctaRes = R.string.widget_open_app_cta,
                scale = scale
            )
            HomeWidgetState.NoActiveChallenge -> IdleContent(
                titleRes = R.string.home_no_challenge_title,
                subtitle = LocalContext.current.getString(R.string.home_no_challenge_subtitle),
                ctaRes = R.string.home_no_challenge_cta,
                characterRes = R.drawable.img_widget_hamster_normal,
                characterHeight = 96.dp,
                scale = scale
            )
        }
    }
}

@Composable
private fun ChallengeProgressContent(challenge: HomeChallenge, scale: WidgetScale) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.bg_widget_card))
            .padding(horizontal = 15.dp.scaled(scale.width), vertical = 13.dp.scaled(scale.height))
    ) {
        ChallengeHeader(challenge, scale)
        Spacer(modifier = GlanceModifier.height(10.dp.scaled(scale.height)))
        ChallengeGauge(challenge, scale)
        Spacer(modifier = GlanceModifier.height(12.dp.scaled(scale.height)))
        SavingsStreak(challenge, scale)
    }
}

/** 4x3 기본 크기에서는 홈 카드의 핵심 정보만 한 화면에 들어오도록 게이지 영역을 가로로 배치한다. */
@Composable
@Suppress("LongMethod") // 홈 카드의 캐릭터·게이지·잔액 계층을 하나의 압축 영역으로 유지한다.
private fun CompactChallengeContent(challenge: HomeChallenge, scale: WidgetScale) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.bg_widget_card))
            .padding(horizontal = 15.dp.scaled(scale.width), vertical = 13.dp.scaled(scale.height))
    ) {
        ChallengeHeader(challenge, scale)
        Spacer(modifier = GlanceModifier.height(7.dp.scaled(scale.height)))
        Row(
            modifier = GlanceModifier.fillMaxWidth().height(128.dp.scaled(scale.height)),
            verticalAlignment = Alignment.Vertical.Bottom
        ) {
            Image(
                provider = ImageProvider(characterDrawableRes(challenge.characterState)),
                contentDescription = context.getString(R.string.cd_hamster_character),
                modifier = GlanceModifier.size(
                    width = 112.dp.scaled(scale.width),
                    height = 124.dp.scaled(scale.height)
                )
            )
            Spacer(modifier = GlanceModifier.width(8.dp.scaled(scale.width)))
            Column(
                modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                PretendardText(
                    text = context.getString(
                        R.string.home_limit_label_format,
                        formatWon(challenge.dailyLimit)
                    ),
                    color = HPText,
                    fontSize = 14.sp.scaled(scale.text),
                    weight = PretendardWeight.Regular,
                    textAlign = WidgetTextAlign.End,
                    maxLines = 1,
                    modifier = GlanceModifier.fillMaxWidth().height(22.dp.scaled(scale.height))
                )
                Spacer(modifier = GlanceModifier.height(6.dp.scaled(scale.height)))
                LinearProgressIndicator(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(10.dp.scaled(scale.height))
                        .cornerRadius(50.dp),
                    progress = challenge.balanceRatio,
                    color = ColorProvider(if (challenge.isOverLimit) HPSub else HPMain),
                    backgroundColor = ColorProvider(HPGray4)
                )
                Spacer(modifier = GlanceModifier.height(10.dp.scaled(scale.height)))
                Row(
                    modifier = GlanceModifier.fillMaxWidth().height(28.dp.scaled(scale.height)),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    PretendardText(
                        text = context.getString(R.string.home_today_balance_label),
                        color = HPText,
                        fontSize = 16.sp.scaled(scale.text),
                        weight = PretendardWeight.Regular,
                        maxLines = 1,
                        modifier = GlanceModifier.width(72.dp.scaled(scale.width)).fillMaxHeight()
                    )
                    PretendardText(
                        text = context.getString(
                            R.string.home_amount_won_format,
                            formatWon(challenge.todayBalance)
                        ),
                        color = if (challenge.isOverLimit) HPSub else HPBlack,
                        fontSize = 20.sp.scaled(scale.text),
                        weight = PretendardWeight.BoldAutoSize,
                        textAlign = WidgetTextAlign.End,
                        maxLines = 1,
                        modifier = GlanceModifier.defaultWeight().fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
@Suppress("LongMethod") // 홈 카드의 두 줄 배너 계층을 그대로 유지한다.
private fun ChallengeHeader(challenge: HomeChallenge, scale: WidgetScale) {
    val context = LocalContext.current
    Row(
        modifier = GlanceModifier.fillMaxWidth().height(28.dp.scaled(scale.height)),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        PretendardText(
            text = context.getString(R.string.home_challenge_in_progress_format, challenge.totalDays),
            color = HPBlack,
            fontSize = 20.sp.scaled(scale.text),
            weight = PretendardWeight.Bold,
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight().fillMaxHeight()
        )
        PretendardText(
            text = context.getString(R.string.home_challenge_detail_link) + "  ›",
            color = HPText,
            fontSize = 14.sp.scaled(scale.text),
            weight = PretendardWeight.Regular,
            textAlign = WidgetTextAlign.End,
            maxLines = 1,
            modifier = GlanceModifier.width(82.dp.scaled(scale.width)).fillMaxHeight()
        )
    }
    Spacer(modifier = GlanceModifier.height(4.dp.scaled(scale.height)))
    Row(
        modifier = GlanceModifier.fillMaxWidth().height(32.dp.scaled(scale.height)),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        PretendardText(
            text = context.getString(
                R.string.home_challenge_period_format,
                challenge.periodStartLabel,
                challenge.periodEndLabel
            ),
            color = HPText,
            fontSize = 14.sp.scaled(scale.text),
            weight = PretendardWeight.Regular,
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight().fillMaxHeight()
        )
        Box(
            modifier = GlanceModifier
                .background(ColorProvider(HPMain))
                .cornerRadius(50.dp)
                .padding(horizontal = 14.dp.scaled(scale.width), vertical = 6.dp.scaled(scale.height))
        ) {
            PretendardText(
                text = if (challenge.dDay == 0) {
                    context.getString(R.string.home_challenge_dday_today)
                } else {
                    context.getString(R.string.home_challenge_dday_format, challenge.dDay)
                },
                color = HPWhite,
                fontSize = 14.sp.scaled(scale.text),
                weight = PretendardWeight.Medium,
                textAlign = WidgetTextAlign.Center,
                maxLines = 1,
                modifier = GlanceModifier.width(52.dp.scaled(scale.width)).height(20.dp.scaled(scale.height))
            )
        }
    }
}

@Composable
private fun ChallengeGauge(challenge: HomeChallenge, scale: WidgetScale) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier.fillMaxWidth().height(190.dp.scaled(scale.height)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Image(
            provider = ImageProvider(characterDrawableRes(challenge.characterState)),
            contentDescription = context.getString(R.string.cd_hamster_character),
            modifier = GlanceModifier.size(
                width = 160.dp.scaled(scale.width),
                height = 180.dp.scaled(scale.height)
            )
        )
    }
    Spacer(modifier = GlanceModifier.height(8.dp.scaled(scale.height)))
    PretendardText(
        text = context.getString(R.string.home_limit_label_format, formatWon(challenge.dailyLimit)),
        color = HPText,
        fontSize = 14.sp.scaled(scale.text),
        weight = PretendardWeight.Regular,
        textAlign = WidgetTextAlign.End,
        maxLines = 1,
        modifier = GlanceModifier.fillMaxWidth().height(22.dp.scaled(scale.height))
    )
    Spacer(modifier = GlanceModifier.height(6.dp.scaled(scale.height)))
    LinearProgressIndicator(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(10.dp.scaled(scale.height))
            .cornerRadius(50.dp),
        progress = challenge.balanceRatio,
        color = ColorProvider(if (challenge.isOverLimit) HPSub else HPMain),
        backgroundColor = ColorProvider(HPGray4)
    )
    Spacer(modifier = GlanceModifier.height(10.dp.scaled(scale.height)))
    Row(
        modifier = GlanceModifier.fillMaxWidth().height(28.dp.scaled(scale.height)),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        PretendardText(
            text = context.getString(R.string.home_today_balance_label),
            color = HPText,
            fontSize = 16.sp.scaled(scale.text),
            weight = PretendardWeight.Regular,
            maxLines = 1,
            modifier = GlanceModifier.width(90.dp.scaled(scale.width)).fillMaxHeight()
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
}

@Composable
@Suppress("LongMethod") // 홈 카드의 두 요약 박스를 한 행 단위로 유지한다.
private fun SavingsStreak(challenge: HomeChallenge, scale: WidgetScale) {
    val context = LocalContext.current
    Row(modifier = GlanceModifier.fillMaxWidth().height(82.dp.scaled(scale.height))) {
        Column(
            modifier = GlanceModifier
                .width(187.dp.scaled(scale.width))
                .fillMaxHeight()
                .background(ImageProvider(R.drawable.bg_widget_savings))
                .padding(vertical = 14.dp.scaled(scale.height)),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            PretendardText(
                text = context.getString(R.string.home_saved_amount_label),
                color = HPText,
                fontSize = 14.sp.scaled(scale.text),
                weight = PretendardWeight.Regular,
                textAlign = WidgetTextAlign.Center,
                maxLines = 1,
                modifier = GlanceModifier.fillMaxWidth().height(22.dp.scaled(scale.height))
            )
            Spacer(modifier = GlanceModifier.height(4.dp.scaled(scale.height)))
            PretendardText(
                text = if (challenge.savedAmount >= 0) {
                    context.getString(R.string.home_saved_amount_format, formatWon(challenge.savedAmount))
                } else {
                    context.getString(R.string.home_saved_amount_negative_format, formatWon(challenge.savedAmount))
                },
                color = if (challenge.savedAmount >= 0) HPMain else HPSub,
                fontSize = 20.sp.scaled(scale.text),
                weight = PretendardWeight.Bold,
                textAlign = WidgetTextAlign.Center,
                maxLines = 1,
                modifier = GlanceModifier.fillMaxWidth().height(28.dp.scaled(scale.height))
            )
        }
        Spacer(modifier = GlanceModifier.width(12.dp.scaled(scale.width)))
        Row(
            modifier = GlanceModifier
                .width(124.dp.scaled(scale.width))
                .fillMaxHeight()
                .background(ImageProvider(R.drawable.bg_widget_streak))
                .padding(horizontal = 6.dp.scaled(scale.width), vertical = 14.dp.scaled(scale.height)),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_fire),
                contentDescription = null,
                modifier = GlanceModifier.size(32.dp.scaled(scale.text))
            )
            Spacer(modifier = GlanceModifier.width(10.dp.scaled(scale.width)))
            Column(modifier = GlanceModifier.defaultWeight()) {
                PretendardText(
                    text = context.getString(R.string.home_streak_label),
                    color = HPWhite,
                    fontSize = 14.sp.scaled(scale.text),
                    weight = PretendardWeight.Regular,
                    maxLines = 1,
                    modifier = GlanceModifier.fillMaxWidth().height(22.dp.scaled(scale.height))
                )
                PretendardText(
                    text = context.getString(R.string.home_streak_days_format, challenge.streakDays),
                    color = HPWhite,
                    fontSize = 20.sp.scaled(scale.text),
                    weight = PretendardWeight.Bold,
                    maxLines = 1,
                    modifier = GlanceModifier.fillMaxWidth().height(28.dp.scaled(scale.height))
                )
            }
        }
    }
}

/** 앱 홈의 챌린지 없음 카드를 재사용하는 로그아웃·로딩·휴식·미진행 상태 화면. */
@Composable
@Suppress("LongMethod", "LongParameterList") // 홈의 텍스트와 우측 하단 캐릭터 계층을 한 카드로 유지한다.
private fun IdleContent(
    titleRes: Int,
    subtitle: String,
    ctaRes: Int,
    characterRes: Int = R.drawable.img_hamster_normal,
    characterHeight: Dp = 100.dp,
    scale: WidgetScale
) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(190.dp.scaled(scale.height))
            .background(ImageProvider(R.drawable.bg_widget_card))
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(horizontal = 20.dp.scaled(scale.width), vertical = 40.dp.scaled(scale.height))
        ) {
            PretendardText(
                text = context.getString(titleRes),
                color = HPBlack,
                fontSize = 20.sp.scaled(scale.text),
                weight = PretendardWeight.BoldAutoSize,
                maxLines = 1,
                modifier = GlanceModifier.fillMaxWidth().height(28.dp.scaled(scale.height))
            )
            Spacer(modifier = GlanceModifier.height(6.dp.scaled(scale.height)))
            PretendardText(
                text = subtitle,
                color = HPText,
                fontSize = 14.sp.scaled(scale.text),
                weight = PretendardWeight.Regular,
                maxLines = 1,
                modifier = GlanceModifier.fillMaxWidth().height(22.dp.scaled(scale.height))
            )
            Spacer(modifier = GlanceModifier.height(24.dp.scaled(scale.height)))
            PretendardText(
                text = context.getString(ctaRes),
                color = HPMain,
                fontSize = 16.sp.scaled(scale.text),
                weight = PretendardWeight.Bold,
                maxLines = 1,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(24.dp.scaled(scale.height))
                    .clickable(actionStartActivity<MainActivity>())
            )
        }
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.Bottom
        ) {
            Spacer(modifier = GlanceModifier.defaultWeight())
            Image(
                provider = ImageProvider(characterRes),
                contentDescription = context.getString(R.string.cd_hamster_character),
                modifier = GlanceModifier.size(
                    width = 110.dp.scaled(scale.width),
                    height = characterHeight.scaled(scale.height)
                )
            )
            Spacer(modifier = GlanceModifier.width(12.dp.scaled(scale.width)))
        }
    }
}

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
import androidx.glance.unit.ColorProvider
import com.example.hampouch.MainActivity
import com.example.hampouch.R
import com.example.hampouch.domain.model.CharacterState
import com.example.hampouch.domain.model.HomeChallenge
import com.example.hampouch.ui.home.components.characterDrawableRes
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray3
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite

/** 홈 화면의 4x2 식비 절약 챌린지 위젯. */
class HomeWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = HomeWidgetSnapshotStore(context).read()
        provideContent { HomeWidgetContent(state) }
    }
}

/** [HomeWidget]을 홈 화면에 등록하는 리시버. */
class HomeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HomeWidget()
}

private val DESIGN_WIDGET_WIDTH = 522.dp
private val DESIGN_WIDGET_HEIGHT = 230.dp

private data class WidgetScale(val width: Float, val height: Float) {
    val text: Float get() = minOf(width, height)
}

@Composable
private fun rememberWidgetScale(): WidgetScale {
    val size = LocalSize.current
    return WidgetScale(
        width = (size.width / DESIGN_WIDGET_WIDTH).coerceAtMost(1f),
        height = (size.height / DESIGN_WIDGET_HEIGHT).coerceAtMost(1f)
    )
}

private fun Dp.scaled(scale: Float): Dp = (value * scale).dp
private fun TextUnit.scaled(scale: Float): TextUnit = (value * scale).sp

@Composable
internal fun HomeWidgetContent(state: HomeWidgetState) {
    val context = LocalContext.current
    val scale = rememberWidgetScale()
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.bg_widget_card))
            .cornerRadius(42.dp.scaled(scale.text))
            .clickable(actionStartActivity<MainActivity>())
    ) {
        when (state) {
            is HomeWidgetState.InProgress -> ChallengeContent(state.challenge, scale)
            HomeWidgetState.LoggedOut -> IdleContent(
                title = context.getString(R.string.widget_login_title),
                subtitle = context.getString(R.string.widget_login_subtitle),
                cta = context.getString(R.string.widget_login_cta),
                scale = scale
            )
            HomeWidgetState.Loading -> IdleContent(
                title = context.getString(R.string.widget_loading_title),
                subtitle = context.getString(R.string.widget_loading_subtitle),
                cta = context.getString(R.string.widget_open_app_cta),
                scale = scale
            )
            is HomeWidgetState.Resting -> IdleContent(
                title = context.getString(R.string.widget_resting_title),
                subtitle = context.getString(
                    R.string.widget_resting_subtitle_format,
                    state.plannedResumeDateLabel
                ),
                cta = context.getString(R.string.widget_open_app_cta),
                scale = scale
            )
            HomeWidgetState.NoActiveChallenge -> IdleContent(
                title = context.getString(R.string.home_no_challenge_title),
                subtitle = context.getString(R.string.home_no_challenge_subtitle),
                cta = context.getString(R.string.home_no_challenge_cta),
                scale = scale
            )
        }
    }
}

@Composable
@Suppress("LongMethod")
private fun ChallengeContent(challenge: HomeChallenge, scale: WidgetScale) {
    val context = LocalContext.current
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(
                horizontal = 20.dp.scaled(scale.width),
                vertical = 25.dp.scaled(scale.height)
            ),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Image(
            provider = ImageProvider(characterDrawableRes(challenge.characterState)),
            contentDescription = context.getString(R.string.cd_hamster_character),
            modifier = GlanceModifier.size(
                width = 160.dp.scaled(scale.width),
                height = 180.dp.scaled(scale.height)
            )
        )
        Column(
            modifier = GlanceModifier.defaultWeight().fillMaxHeight()
                .padding(vertical = 10.dp.scaled(scale.height))
        ) {
            ChallengeHeader(challenge, scale)
            Column(
                modifier = GlanceModifier.defaultWeight().fillMaxWidth()
                    .padding(start = 7.dp.scaled(scale.width)),
                verticalAlignment = Alignment.Vertical.Bottom
            ) {
                BalanceContent(challenge, scale)
            }
        }
    }
}

@Composable
private fun ChallengeHeader(challenge: HomeChallenge, scale: WidgetScale) {
    val context = LocalContext.current
    Row(
        modifier = GlanceModifier.fillMaxWidth().height(48.dp.scaled(scale.height)),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Column(
            modifier = GlanceModifier.defaultWeight().fillMaxHeight()
                .padding(start = 10.dp.scaled(scale.width)),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            PretendardText(
                text = context.getString(
                    R.string.home_challenge_in_progress_format,
                    challenge.totalDays
                ),
                color = HPBlack,
                fontSize = 16.sp.scaled(scale.text),
                weight = PretendardWeight.SemiBold16AutoSize,
                maxLines = 1,
                modifier = GlanceModifier.fillMaxWidth().height(26.dp.scaled(scale.height))
            )
            PretendardText(
                text = context.getString(
                    R.string.home_challenge_period_format,
                    challenge.periodStartLabel,
                    challenge.periodEndLabel
                ),
                color = HPText,
                fontSize = 14.sp.scaled(scale.text),
                weight = PretendardWeight.Medium,
                maxLines = 1,
                modifier = GlanceModifier.fillMaxWidth().height(22.dp.scaled(scale.height))
            )
        }
        Box(
            modifier = GlanceModifier
                .width(136.dp.scaled(scale.width))
                .height(39.dp.scaled(scale.height))
                .background(ColorProvider(HPMain))
                .cornerRadius(10.dp.scaled(scale.text)),
            contentAlignment = Alignment.Center
        ) {
            PretendardText(
                text = "${context.getString(R.string.home_streak_label)} ${
                    context.getString(R.string.home_streak_days_format, challenge.streakDays)
                }",
                color = HPWhite,
                fontSize = 16.sp.scaled(scale.text),
                weight = PretendardWeight.SemiBold,
                textAlign = WidgetTextAlign.Center,
                maxLines = 1,
                modifier = GlanceModifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun BalanceContent(challenge: HomeChallenge, scale: WidgetScale) {
    val context = LocalContext.current
    Row(
        modifier = GlanceModifier.fillMaxWidth().height(36.dp.scaled(scale.height)),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        PretendardText(
            text = context.getString(R.string.home_today_balance_label),
            color = HPBlack,
            fontSize = 16.sp.scaled(scale.text),
            weight = PretendardWeight.Medium,
            maxLines = 1,
            modifier = GlanceModifier.width(82.dp.scaled(scale.width)).fillMaxHeight()
        )
        PretendardText(
            text = context.getString(
                R.string.home_amount_won_format,
                formatWon(challenge.todayBalance)
            ),
            color = HPBlack,
            fontSize = 28.sp.scaled(scale.text),
            weight = PretendardWeight.BoldAutoSize,
            textAlign = WidgetTextAlign.End,
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight().fillMaxHeight()
        )
    }
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(16.dp.scaled(scale.height))
            .background(ColorProvider(HPWhite))
            .cornerRadius(10.dp.scaled(scale.text))
            .padding(1.dp.scaled(scale.text))
    ) {
        LinearProgressIndicator(
            modifier = GlanceModifier.fillMaxSize().cornerRadius(10.dp.scaled(scale.text)),
            progress = challenge.balanceRatio,
            color = ColorProvider(progressColor(challenge.characterState)),
            backgroundColor = ColorProvider(HPGray3)
        )
    }
    Spacer(modifier = GlanceModifier.height(9.dp.scaled(scale.height)))
    PretendardText(
        text = context.getString(
            R.string.home_limit_label_format,
            formatWon(challenge.dailyLimit)
        ),
        color = HPText,
        fontSize = 14.sp.scaled(scale.text),
        weight = PretendardWeight.Medium,
        textAlign = WidgetTextAlign.End,
        maxLines = 1,
        modifier = GlanceModifier.fillMaxWidth().height(22.dp.scaled(scale.height))
    )
}

private fun progressColor(state: CharacterState) = when (state) {
    CharacterState.CHUBBY -> HPSub2
    CharacterState.NORMAL -> HPMain
    CharacterState.THIN, CharacterState.OVER_LIMIT -> HPSub
}

@Composable
@Suppress("LongMethod")
private fun IdleContent(
    title: String,
    subtitle: String,
    cta: String,
    scale: WidgetScale
) {
    val context = LocalContext.current
    Box(modifier = GlanceModifier.fillMaxSize()) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(
                    start = 30.dp.scaled(scale.width),
                    top = 55.dp.scaled(scale.height)
                )
        ) {
            PretendardText(
                text = title,
                color = HPBlack,
                fontSize = 24.sp.scaled(scale.text),
                weight = PretendardWeight.SemiBold24AutoSize,
                maxLines = 1,
                modifier = GlanceModifier
                    .width(462.dp.scaled(scale.width))
                    .height(32.dp.scaled(scale.height))
            )
            PretendardText(
                text = subtitle,
                color = HPText,
                fontSize = 18.sp.scaled(scale.text),
                weight = PretendardWeight.Medium18AutoSize,
                maxLines = 1,
                modifier = GlanceModifier
                    .width(302.dp.scaled(scale.width))
                    .height(26.dp.scaled(scale.height))
            )
            Spacer(modifier = GlanceModifier.height(15.dp.scaled(scale.height)))
            PretendardText(
                text = cta,
                color = HPSub1,
                fontSize = 16.sp.scaled(scale.text),
                weight = PretendardWeight.SemiBold,
                maxLines = 1,
                modifier = GlanceModifier
                    .width(302.dp.scaled(scale.width))
                    .height(24.dp.scaled(scale.height))
            )
        }
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.Bottom
        ) {
            Spacer(modifier = GlanceModifier.defaultWeight())
            Image(
                provider = ImageProvider(R.drawable.img_widget_hamster_normal),
                contentDescription = context.getString(R.string.cd_hamster_character),
                modifier = GlanceModifier.size(
                    width = 160.dp.scaled(scale.width),
                    height = 139.dp.scaled(scale.height)
                )
            )
            Spacer(modifier = GlanceModifier.width(30.dp.scaled(scale.width)))
        }
    }
}

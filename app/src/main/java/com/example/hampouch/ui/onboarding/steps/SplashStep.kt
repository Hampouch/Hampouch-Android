package com.example.hampouch.ui.onboarding.steps

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.hampouch.R
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HampouchTheme
import kotlinx.coroutines.delay

private const val SplashFadeInDurationMillis = 900
private const val SplashHoldAfterFadeMillis = 1100

@Composable
fun SplashStep(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    val logoAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = SplashFadeInDurationMillis),
        label = "splash_logo_alpha"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay((SplashFadeInDurationMillis + SplashHoldAfterFadeMillis).toLong())
        onTimeout()
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(HPSub2, Color(0xFFE3D7B8))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.title_logo),
                contentDescription = stringResource(R.string.cd_hampouch_logo),
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .alpha(logoAlpha)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashStepPreview() {
    HampouchTheme {
        SplashStep(onTimeout = {})
    }
}

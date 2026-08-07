package com.example.hampouch.ui.onboarding.steps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.R
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import kotlinx.coroutines.delay

private const val LoadingDurationMillis = 2000L
private const val LoadingDotIntervalMillis = 400L
private const val LoadingMaxDotCount = 3

@Composable
fun LoadingStep(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        delay(LoadingDurationMillis)
        onTimeout()
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HPWhite)
                .background(
                    Brush.radialGradient(colors = listOf(HPSub2.copy(alpha = 0.35f), Color.Transparent))
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.img_hamster_normal),
                    contentDescription = stringResource(R.string.cd_hamster_mascot),
                    modifier = Modifier.size(200.dp)
                )
                AnimatedLoadingText(baseText = stringResource(R.string.onboarding_loading_label))
            }
        }
    }
}

@Composable
private fun AnimatedLoadingText(
    baseText: String,
    modifier: Modifier = Modifier
) {
    var dotCount by remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(LoadingDotIntervalMillis)
            dotCount = if (dotCount >= LoadingMaxDotCount) 1 else dotCount + 1
        }
    }

    Text(
        text = baseText + ".".repeat(dotCount),
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 40.sp),
        color = HPSub1,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun LoadingStepPreview() {
    HampouchTheme {
        LoadingStep(onTimeout = {})
    }
}

package com.example.hampouch.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = HPMain,
    secondary = HPSub1,
    tertiary = HPSub2,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = HPSub1,
    onSurface = HPSub1,
)

private val LightColorScheme = lightColorScheme(
    primary = HPMain,
    secondary = HPSub1,
    tertiary = HPSub2,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = HPSub1,
    onSurface = HPSub1,
)

@Composable
fun HampouchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
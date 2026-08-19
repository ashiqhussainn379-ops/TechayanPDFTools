package com.techayan.facebookclone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalFacebookColors = staticCompositionLocalOf { facebookColors(false) }

private val LightScheme = lightColorScheme(
    primary = FacebookBlue,
    onPrimary = Color.White,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightText,
    onSurface = LightText,
)

private val DarkScheme = darkColorScheme(
    primary = FacebookBlueDark,
    onPrimary = Color.White,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkText,
    onSurface = DarkText,
)

@Composable
fun FacebookTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val colors = facebookColors(darkTheme)
    CompositionLocalProvider(LocalFacebookColors provides colors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = Typography,
            content = content,
        )
    }
}

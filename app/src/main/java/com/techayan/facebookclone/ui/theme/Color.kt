package com.techayan.facebookclone.ui.theme

import androidx.compose.ui.graphics.Color

val FacebookBlue = Color(0xFF1877F2)
val FacebookBlueDark = Color(0xFF0866FF)
val MessengerBlue = Color(0xFF0084FF)
val OnlineGreen = Color(0xFF31A24C)
val CreateGreen = Color(0xFF42B72A)
val LikeBlue = Color(0xFF0866FF)
val LoveRed = Color(0xFFF02849)
val CareOrange = Color(0xFFF7B125)
val DividerLight = Color(0xFFCED0D4)
val IconGray = Color(0xFF65676B)

val LightBackground = Color(0xFFF0F2F5)
val LightSurface = Color(0xFFFFFFFF)
val LightText = Color(0xFF050505)
val LightTextSecondary = Color(0xFF65676B)
val LightIconButton = Color(0xFFE4E6EB)

val DarkBackground = Color(0xFF18191A)
val DarkSurface = Color(0xFF242526)
val DarkText = Color(0xFFE4E6EB)
val DarkTextSecondary = Color(0xFFB0B3B8)
val DarkIconButton = Color(0xFF3A3B3C)
val DarkDivider = Color(0xFF3E4042)

data class FacebookColors(
    val background: Color,
    val surface: Color,
    val text: Color,
    val textSecondary: Color,
    val iconButton: Color,
    val divider: Color,
    val accent: Color,
    val isDark: Boolean,
)

fun facebookColors(dark: Boolean) = if (dark) {
    FacebookColors(
        background = DarkBackground,
        surface = DarkSurface,
        text = DarkText,
        textSecondary = DarkTextSecondary,
        iconButton = DarkIconButton,
        divider = DarkDivider,
        accent = FacebookBlueDark,
        isDark = true,
    )
} else {
    FacebookColors(
        background = LightBackground,
        surface = LightSurface,
        text = LightText,
        textSecondary = LightTextSecondary,
        iconButton = LightIconButton,
        divider = DividerLight,
        accent = FacebookBlue,
        isDark = false,
    )
}

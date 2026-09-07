package com.wideawake.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryOrange,
    secondary = AccentYellow,
    tertiary = LaserGreen,
    background = Black,
    surface = DarkSurface,
    onPrimary = Black,
    onSecondary = Black,
    onBackground = White,
    onSurface = White,
    error = PenaltyRed
)

@Composable
fun WakeOrPayTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

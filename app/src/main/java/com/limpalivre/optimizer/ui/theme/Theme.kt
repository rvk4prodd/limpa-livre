package com.limpalivre.optimizer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color(0xFF042F22),
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = EmeraldSoft,
    secondary = CyanAccent,
    onSecondary = Color(0xFF042B35),
    secondaryContainer = Color(0xFF0E3A47),
    onSecondaryContainer = Color(0xFF67E8F9),
    tertiary = AmberWarning,
    background = BgPrimary,
    onBackground = TextPrimary,
    surface = BgCard,
    onSurface = TextPrimary,
    surfaceVariant = BgCardElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    error = CoralDanger,
    onError = Color.White
)

@Composable
fun LimpaLivreTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = LimpaLivreTypography,
        content = content
    )
}

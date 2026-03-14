package com.example.motes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MotesLightColorScheme = lightColorScheme(
    primary = Accent,
    onPrimary = DarkBlueBase,
    background = LightBackground,
    onBackground = DarkBlueBase,
    surface = LightSurface,
    onSurface = DarkBlueBase,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = DarkBlueBase.copy(alpha = 0.72f),
    outline = DarkBlueBase.copy(alpha = 0.4f),
    tertiary = LightSurface
)

private val MotesDarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = PrimaryBackground,
    background = PrimaryBackground,
    onBackground = TextPrimary,
    surface = SurfaceLow,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMedium,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    tertiary = SurfaceHigh
)

private val MotesPeachColorScheme = lightColorScheme(
    primary = PeachAccent,
    onPrimary = DarkBlueBase,
    background = PeachBackground,
    onBackground = DarkBlueBase,
    surface = Color(0xFFFFF6E7),
    onSurface = DarkBlueBase,
    surfaceVariant = Color(0xFFF7DFC0),
    onSurfaceVariant = DarkBlueBase.copy(alpha = 0.74f),
    outline = PeachAccent.copy(alpha = 0.35f),
    tertiary = Color(0xFFFFF1DB)
)

private val MotesSeaColorScheme = lightColorScheme(
    primary = SeaAccent,
    onPrimary = Color.White,
    background = Color(0xFFE7F3F4),
    onBackground = DarkBlueBase,
    surface = Color(0xFFF4FBFB),
    onSurface = DarkBlueBase,
    surfaceVariant = Color(0xFFD5EAEC),
    onSurfaceVariant = DarkBlueBase.copy(alpha = 0.74f),
    outline = SeaAccent.copy(alpha = 0.32f),
    tertiary = Color(0xFFEAF6F7)
)

@Composable
fun usesLightCardPalette(): Boolean {
    return when (AppThemeState.mode) {
        ThemeMode.DARK -> false
        ThemeMode.SYSTEM -> !isSystemInDarkTheme()
        ThemeMode.LIGHT, ThemeMode.PEACH, ThemeMode.SEA -> true
    }
}

@Composable
fun MotesTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = when (AppThemeState.mode) {
        ThemeMode.DARK -> MotesDarkColorScheme
        ThemeMode.LIGHT -> MotesLightColorScheme
        ThemeMode.SYSTEM -> if (isSystemInDarkTheme()) MotesDarkColorScheme else MotesLightColorScheme
        ThemeMode.PEACH -> MotesPeachColorScheme
        ThemeMode.SEA -> MotesSeaColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}

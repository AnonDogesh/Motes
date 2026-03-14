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
    surface = PeachBackground.copy(alpha = 0.84f),
    onSurface = DarkBlueBase,
    surfaceVariant = PeachBackground.copy(alpha = 0.72f),
    onSurfaceVariant = DarkBlueBase.copy(alpha = 0.72f),
    outline = PeachAccent.copy(alpha = 0.45f),
    tertiary = PeachBackground.copy(alpha = 0.90f)
)

private val MotesSeaColorScheme = lightColorScheme(
    primary = SeaAccent,
    onPrimary = Color.White,
    background = SeaBackground,
    onBackground = DarkBlueBase,
    surface = SeaBackground.copy(alpha = 0.84f),
    onSurface = DarkBlueBase,
    surfaceVariant = SeaBackground.copy(alpha = 0.72f),
    onSurfaceVariant = DarkBlueBase.copy(alpha = 0.72f),
    outline = SeaAccent.copy(alpha = 0.45f),
    tertiary = SeaBackground.copy(alpha = 0.90f)
)


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

package com.example.motes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


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

@Composable
fun MotesTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (AppThemeState.mode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (useDarkTheme) MotesDarkColorScheme else MotesLightColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}

package com.example.motes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

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
    // Dark-first design system: app defaults to the custom dark palette.
    MaterialTheme(
        colorScheme = MotesDarkColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}

package com.example.motes.ui.theme

import androidx.compose.ui.graphics.Color

// Brand palette base colors
val DarkBlueBase = Color(0xFF25343F)
private val BrandTextPrimary = Color(0xFFEAEFEF)
private val BrandTextSecondary = Color(0xFFBFC9D1)
private val BrandAccent = Color(0xFFFF9B51)

val LightBackground = Color(0xFFF5EEDC)
val LightSurface = Color(0xFFEFE5D0)
val LightSurfaceVariant = Color(0xFFE3D8C1)

/**
 * App design-system tokens.
 *
 * Tonal layering note:
 * - PrimaryBackground is the darkest base layer.
 * - SurfaceLow/Medium/High are progressively lighter overlays used for elevation cues.
 * - BorderSubtle stays close to surfaces to avoid high-contrast outlines in dark UI.
 */
val PrimaryBackground = DarkBlueBase
val SurfaceLow = Color(0xFF2C3D49)
val SurfaceMedium = Color(0xFF324754)
val SurfaceHigh = Color(0xFF395161)

val Accent = BrandAccent
val TextPrimary = BrandTextPrimary
val TextSecondary = BrandTextSecondary
val BorderSubtle = Color(0xFF4A5B67)

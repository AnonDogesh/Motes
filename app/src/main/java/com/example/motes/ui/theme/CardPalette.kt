package com.example.motes.ui.theme

val DarkCardPalette: List<Long> = listOf(
    0xFF6B5E2EL, 0xFF7A4A2BL, 0xFF6A3B3BL, 0xFF5A3F6EL,
    0xFF3F4F74L, 0xFF2F5D78L, 0xFF2F6F6DL, 0xFF3E6B3EL,
    0xFF5E6A2EL, 0xFF6B6B2EL, 0xFF5C4A3BL, 0xFF4E5B63L
)

val LightCardPalette: List<Long> = listOf(
    0xFFD8CC9AL, 0xFFE0C0A5L, 0xFFDBB3B3L, 0xFFCDB6E0L,
    0xFFB9C6E3L, 0xFFB5D3E0L, 0xFFB5DDD9L, 0xFFC3DABCL,
    0xFFD7DEB2L, 0xFFE1DEB3L, 0xFFDCC9BFL, 0xFFC8D3DAL
)

private val darkToLight: Map<Long, Long> = DarkCardPalette.zip(LightCardPalette).toMap()
private val lightToDark: Map<Long, Long> = LightCardPalette.zip(DarkCardPalette).toMap()

fun cardColorForDisplay(storedColor: Long, isLightTheme: Boolean): Long {
    return if (isLightTheme) {
        darkToLight[storedColor] ?: storedColor
    } else {
        lightToDark[storedColor] ?: storedColor
    }
}

fun cardColorForStorage(selectedColor: Long, isLightTheme: Boolean): Long {
    // Persist canonical dark palette so existing notes transform automatically with theme changes.
    return if (isLightTheme) {
        lightToDark[selectedColor] ?: selectedColor
    } else {
        lightToDark[selectedColor] ?: selectedColor
    }
}

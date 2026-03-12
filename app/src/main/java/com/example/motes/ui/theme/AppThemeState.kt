package com.example.motes.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

object AppThemeState {
    var mode by mutableStateOf(ThemeMode.DARK)
}

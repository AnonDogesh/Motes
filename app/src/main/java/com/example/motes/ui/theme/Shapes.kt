package com.example.motes.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(20.dp), // Required rounded corners for cards/surfaces.
    large = RoundedCornerShape(28.dp)
)

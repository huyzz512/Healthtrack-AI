package com.healthtrackai.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object HealthPalette {
    val Background: Color
        @Composable get() = MaterialTheme.colorScheme.background
    val Card: Color
        @Composable get() = MaterialTheme.colorScheme.surface
    val Border: Color
        @Composable get() = MaterialTheme.colorScheme.outlineVariant
    val Primary: Color
        @Composable get() = MaterialTheme.colorScheme.primary
    val Mint: Color
        @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val TextPrimary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface
    val TextSecondary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val Divider: Color
        @Composable get() = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .45f)
    val ActivityBackground = Color(0xFFFFF1F2)
    val ActivityAccent = Color(0xFFE11D48)
    val MoodBackground = Color(0xFFECFDF5)
    val MoodAccent = Color(0xFF059669)
    val NutritionBackground = Color(0xFFEFF6FF)
    val NutritionAccent = Color(0xFF2563EB)
    val SleepBackground = Color(0xFFF5F3FF)
    val SleepAccent = Color(0xFF7C3AED)
}

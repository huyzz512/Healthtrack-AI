package com.healthtrackai.app.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HealthTrackColors = lightColorScheme(
    primary = Color(0xFF006C51),
    onPrimary = Color.White,
    secondary = Color(0xFF4C635A),
    onSecondary = Color.White,
    primaryContainer = Color(0xFFD3F0E3),
    onPrimaryContainer = Color(0xFF002117),
    secondaryContainer = Color(0xFFE1F1E9),
    onSecondaryContainer = Color(0xFF10241B),
    surfaceVariant = Color(0xFFECF4EF),
    onSurfaceVariant = Color(0xFF4F6258),
    outline = Color(0xFF84958B),
    outlineVariant = Color(0xFFD4DED7),
    background = Color(0xFFF6FBF7),
    surface = Color(0xFFF6FBF7),
)

@Composable
fun HealthTrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HealthTrackColors,
        content = content,
    )
}

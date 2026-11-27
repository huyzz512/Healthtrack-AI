package com.healthtrackai.app.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HealthTrackColors = lightColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = Color.White,
    secondary = Color(0xFF0F766E),
    onSecondary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF134E4A),
    secondaryContainer = Color(0xFFECFDF5),
    onSecondaryContainer = Color(0xFF065F46),
    surfaceVariant = Color(0xFFEFF4F8),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFF84958B),
    outlineVariant = Color(0xFFD4DED7),
    background = Color(0xFFF7F9FC),
    surface = Color(0xFFFFFFFF),
)

@Composable
fun HealthTrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HealthTrackColors,
        content = content,
    )
}

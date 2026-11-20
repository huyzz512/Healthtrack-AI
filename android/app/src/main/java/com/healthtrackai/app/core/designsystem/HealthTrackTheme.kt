package com.healthtrackai.app.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HealthTrackColors = lightColorScheme(
    primary = Color(0xFF006C51),
    onPrimary = Color.White,
    secondary = Color(0xFF4C635A),
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

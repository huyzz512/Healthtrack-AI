package com.healthtrackai.app.core.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

@Composable
fun HealthTrackTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val background by animateColorAsState(
        targetValue = if (darkTheme) Color(0xFF0B1120) else Color(0xFFF7F9FC),
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "theme-background",
    )
    val surface by animateColorAsState(
        targetValue = if (darkTheme) Color(0xFF111B2E) else Color.White,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "theme-surface",
    )
    val surfaceVariant by animateColorAsState(
        targetValue = if (darkTheme) Color(0xFF1E293B) else Color(0xFFEFF4F8),
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "theme-surface-variant",
    )
    val primary by animateColorAsState(
        targetValue = if (darkTheme) Color(0xFF5EEAD4) else Color(0xFF0F766E),
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "theme-primary",
    )
    val onPrimary by animateColorAsState(
        targetValue = if (darkTheme) Color(0xFF042F2E) else Color.White,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "theme-on-primary",
    )
    val onSurface by animateColorAsState(
        targetValue = if (darkTheme) Color(0xFFE2E8F0) else Color(0xFF0F172A),
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "theme-on-surface",
    )
    val onSurfaceVariant by animateColorAsState(
        targetValue = if (darkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "theme-on-surface-variant",
    )
    val outline by animateColorAsState(
        targetValue = if (darkTheme) Color(0xFF475569) else Color(0xFF84958B),
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "theme-outline",
    )
    val outlineVariant by animateColorAsState(
        targetValue = if (darkTheme) Color(0xFF334155) else Color(0xFFD4DED7),
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "theme-outline-variant",
    )
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            secondary = primary,
            onSecondary = onPrimary,
            primaryContainer = Color(0xFF134E4A),
            onPrimaryContainer = Color(0xFFCCFBF1),
            secondaryContainer = Color(0xFF12352F),
            onSecondaryContainer = Color(0xFFCCFBF1),
            background = background,
            onBackground = onSurface,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            secondary = primary,
            onSecondary = onPrimary,
            primaryContainer = Color(0xFFCCFBF1),
            onPrimaryContainer = Color(0xFF134E4A),
            secondaryContainer = Color(0xFFECFDF5),
            onSecondaryContainer = Color(0xFF065F46),
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            background = background,
            onBackground = onSurface,
            surface = surface,
            onSurface = onSurface,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

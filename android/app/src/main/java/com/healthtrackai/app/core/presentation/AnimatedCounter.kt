package com.healthtrackai.app.core.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import kotlin.math.roundToInt

@Composable
fun AnimatedCounter(
    value: Int,
    suffix: String = "",
    modifier: Modifier = Modifier,
    style: TextStyle,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Clip,
    animationKey: Any? = Unit,
) {
    val animatedValue by animateFromZeroFloatAsState(
        targetValue = value.toFloat(),
        animationKey = animationKey,
        label = "animated-counter",
    )
    BasicText(
        text = formatVietnameseInteger(animatedValue.roundToInt()) + suffix,
        modifier = modifier,
        style = style,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun animateFromZeroFloatAsState(
    targetValue: Float,
    animationKey: Any? = Unit,
    label: String,
): State<Float> {
    var animatedTarget by remember(animationKey) { mutableFloatStateOf(0f) }
    LaunchedEffect(targetValue, animationKey) {
        animatedTarget = targetValue
    }
    return animateFloatAsState(
        targetValue = animatedTarget,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = label,
    )
}

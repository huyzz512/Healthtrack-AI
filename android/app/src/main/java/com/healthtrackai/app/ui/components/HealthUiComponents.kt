package com.healthtrackai.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthtrackai.app.ui.theme.HealthPalette

@Composable
fun HealthScreenHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = HealthPalette.TextPrimary, maxLines = 1)
        Text(subtitle, fontSize = 14.sp, color = HealthPalette.TextSecondary, maxLines = 2)
    }
}

@Composable
fun DemoBadge(text: String = "DỮ LIỆU MẪU", modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = CircleShape, color = HealthPalette.Mint) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = HealthPalette.Primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HealthProgressBar(progress: Float, color: Color, modifier: Modifier = Modifier) {
    androidx.compose.material3.LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier,
        color = color,
        trackColor = color.copy(alpha = .14f),
    )
}

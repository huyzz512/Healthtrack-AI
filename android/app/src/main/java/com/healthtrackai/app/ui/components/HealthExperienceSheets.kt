package com.healthtrackai.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val PulseRed = Color(0xFFE11D48)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulseSimulatorSheet(onDismiss: () -> Unit) {
    var requestId by remember { mutableIntStateOf(0) }
    var secondsRemaining by remember { mutableIntStateOf(0) }
    var isMeasuring by remember { mutableStateOf(false) }
    var hasResult by remember { mutableStateOf(false) }
    val pulseTransition = rememberInfiniteTransition(label = "pulse-simulator")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-simulator-scale",
    )
    val wavePhase by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulse-ecg-phase",
    )

    LaunchedEffect(requestId) {
        if (requestId > 0) {
            hasResult = false
            isMeasuring = true
            for (second in 5 downTo 1) {
                secondsRemaining = second
                delay(1_000)
            }
            secondsRemaining = 0
            isMeasuring = false
            hasResult = true
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Mô phỏng đo nhịp tim", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                when {
                    isMeasuring -> "Giữ yên thiết bị · còn $secondsRemaining giây"
                    hasResult -> "Kết quả mô phỏng"
                    else -> "Chạm bắt đầu để chạy phiên đo 5 giây"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Box(
                Modifier.size(112.dp)
                    .scale(if (isMeasuring) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF1F2)),
                contentAlignment = Alignment.Center,
            ) {
                if (isMeasuring) {
                    CircularProgressIndicator(
                        progress = { (5 - secondsRemaining) / 5f },
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        color = PulseRed,
                        trackColor = PulseRed.copy(alpha = .14f),
                        strokeWidth = 4.dp,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    androidx.compose.material3.Icon(
                        Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = PulseRed,
                        modifier = Modifier.size(34.dp),
                    )
                    if (hasResult) {
                        Text("72", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = PulseRed)
                    } else if (isMeasuring) {
                        Text(secondsRemaining.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PulseRed)
                    }
                }
            }
            EcgWaveform(phase = wavePhase)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Nhịp tim", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    if (hasResult) "72 nhịp/phút" else if (isMeasuring) "Đang phân tích" else "Chưa đo",
                    fontWeight = FontWeight.Bold,
                    color = if (hasResult) PulseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    "Đây là mô phỏng giao diện bằng dữ liệu mẫu, không đọc cảm biến và không dùng để đánh giá sức khỏe.",
                    Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp,
                )
            }
            Button(
                onClick = { requestId += 1 },
                enabled = !isMeasuring,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text(if (hasResult) "Đo lại trong 5 giây" else "Bắt đầu đo 5 giây")
            }
        }
    }
}

@Composable
private fun EcgWaveform(phase: Float) {
    Canvas(
        Modifier.fillMaxWidth()
            .height(88.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        val centerY = size.height / 2f
        val grid = Color(0xFF94A3B8).copy(alpha = .18f)
        val gridStep = 24.dp.toPx()
        var gridX = 0f
        while (gridX < size.width) {
            drawLine(grid, Offset(gridX, 0f), Offset(gridX, size.height), strokeWidth = 1.dp.toPx())
            gridX += gridStep
        }
        for (row in 0..3) {
            val y = size.height * row / 3f
            drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }

        val cycleWidth = 190.dp.toPx()
        val points = listOf(
            0f to 0f,
            .12f to 0f,
            .17f to -.08f,
            .20f to .10f,
            .24f to -.48f,
            .28f to .72f,
            .32f to -.18f,
            .37f to 0f,
            .62f to 0f,
            .67f to -.06f,
            .72f to 0f,
            1f to 0f,
        )
        var cycleStart = -phase * cycleWidth
        while (cycleStart < size.width + cycleWidth) {
            val path = Path()
            points.forEachIndexed { index, point ->
                val x = cycleStart + point.first * cycleWidth
                val y = centerY + point.second * size.height * .72f
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = PulseRed,
                style = Stroke(width = 2.5.dp.toPx()),
            )
            cycleStart += cycleWidth
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthCoachSheet(onDismiss: () -> Unit) {
    val questions = listOf(
        "Hôm nay tôi nên ăn gì?",
        "Phân tích giấc ngủ tuần này",
        "Làm sao để uống đủ nước?",
    )
    var selectedQuestion by remember { mutableStateOf<String?>(null) }
    var requestId by remember { mutableIntStateOf(0) }
    var typingDots by remember { mutableIntStateOf(1) }
    var isTyping by remember { mutableStateOf(false) }
    var hasAnswer by remember { mutableStateOf(false) }

    LaunchedEffect(requestId) {
        if (requestId > 0) {
            isTyping = true
            hasAnswer = false
            repeat(5) { index ->
                typingDots = index % 3 + 1
                delay(320)
            }
            isTyping = false
            hasAnswer = true
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Trợ lý sức khỏe AI", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("Chọn câu hỏi để xem phân tích minh họa.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            questions.forEach { question ->
                Surface(
                    Modifier.fillMaxWidth().clickable {
                        selectedQuestion = question
                        requestId += 1
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        question,
                        Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            selectedQuestion?.let { question ->
                Surface(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text("Bạn hỏi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(question, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        when {
                            isTyping -> Text(
                                "HealthTrack AI đang nhập${".".repeat(typingDots)}",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            hasAnswer -> {
                                Text(
                                    coachAnswer(question),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp,
                                )
                                CoachMiniChart()
                                Text(
                                    "Gợi ý tham khảo từ dữ liệu mẫu, không thay thế tư vấn chuyên môn.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

private fun coachAnswer(question: String): String = when {
    question.startsWith("Hôm nay") ->
        "Hãy ưu tiên một bữa ăn cân bằng có rau, nguồn đạm phù hợp và tinh bột nguyên hạt. Điều chỉnh khẩu phần theo nhu cầu cá nhân."
    question.startsWith("Phân tích") ->
        "Thời lượng ngủ mẫu tuần này khá ổn định. Bạn có thể duy trì giờ ngủ đều đặn và giảm thiết bị điện tử trước khi nghỉ."
    else ->
        "Hãy chia lượng nước thành nhiều lần trong ngày và đặt nhắc nhở nhẹ nhàng. Nhu cầu thực tế tùy thuộc cơ thể và hoạt động."
}

@Composable
private fun CoachMiniChart() {
    val barHeights = listOf(24, 35, 29, 42, 33, 48, 39)
    Row(
        Modifier.fillMaxWidth()
            .height(74.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        barHeights.forEachIndexed { index, height ->
            val barColor = if (index == barHeights.lastIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = .38f)
            Spacer(
                Modifier.weight(1f)
                    .height(height.dp)
                    .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                    .background(barColor),
            )
        }
    }
}

@Composable
fun ConfettiCelebration(active: Boolean, onFinished: () -> Unit) {
    val progress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = tween(2_200, easing = FastOutSlowInEasing),
        label = "confetti-progress",
    )
    LaunchedEffect(active) {
        if (active) {
            delay(2_300)
            onFinished()
        }
    }
    if (active) {
        Canvas(Modifier.fillMaxSize()) {
            val colors = listOf(
                Color(0xFF0F766E),
                Color(0xFF2563EB),
                Color(0xFFE11D48),
                Color(0xFFF59E0B),
                Color(0xFF7C3AED),
            )
            repeat(26) { index ->
                val x = size.width * ((index * 37 % 100) / 100f)
                val speed = .36f + (index % 5) * .12f
                val y = -18f + size.height * progress * speed
                val rotation = (index * 41 + progress * 300f) % 360f
                val pieceSize = Size(7.dp.toPx(), 13.dp.toPx())
                rotate(rotation, pivot = Offset(x, y)) {
                    drawRect(
                        color = colors[index % colors.size].copy(alpha = (1f - progress * .45f).coerceIn(0f, 1f)),
                        topLeft = Offset(x, y),
                        size = pieceSize,
                    )
                }
            }
        }
    }
}

package com.healthtrackai.app.ui.screens.overview

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MedicalInformation
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthtrackai.app.core.presentation.FoundationFeature
import com.healthtrackai.app.core.presentation.AnimatedCounter
import com.healthtrackai.app.core.presentation.animateFromZeroFloatAsState
import com.healthtrackai.app.core.presentation.bouncyClick
import com.healthtrackai.app.core.presentation.formatVietnameseInteger
import com.healthtrackai.app.ui.components.ConfettiCelebration
import com.healthtrackai.app.ui.components.HealthCoachSheet
import com.healthtrackai.app.ui.components.PulseSimulatorSheet
import com.healthtrackai.app.feature.dashboard.DashboardUiEvent
import com.healthtrackai.app.feature.dashboard.DashboardUiState
import com.healthtrackai.app.feature.dashboard.BackendStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private val Teal: Color
    @Composable get() = MaterialTheme.colorScheme.primary
private val Ink: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val Muted: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

@Composable
private fun metricCardColor(light: Color, dark: Color): Color =
    if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) dark else light

@Composable
private fun metricAccentColor(light: Color, dark: Color): Color =
    if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) dark else light

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun OverviewScreen(state: DashboardUiState, onEvent: (DashboardUiEvent) -> Unit, onNavigate: (FoundationFeature) -> Unit, modifier: Modifier = Modifier) {
    var quickAddVisible by remember { mutableStateOf(false) }
    var selectedQuickAdd by remember { mutableStateOf<String?>(null) }
    var steps by remember { mutableStateOf(6_420) }
    var sleepMinutes by remember { mutableStateOf(450) }
    var mood by remember { mutableStateOf("Tích cực") }
    var weightDialogVisible by remember { mutableStateOf(false) }
    var weightInput by remember { mutableStateOf("68.0") }
    var weightInputError by remember { mutableStateOf(false) }
    var heartRateSheetVisible by remember { mutableStateOf(false) }
    var aiCoachVisible by remember { mutableStateOf(false) }
    var celebrationActive by remember { mutableStateOf(false) }
    var stepGoalCelebrated by remember { mutableStateOf(false) }
    var waterWasBelowGoal by remember { mutableStateOf(state.waterIntakeMl < 2_000) }
    var waterGoalCelebrated by remember { mutableStateOf(false) }
    var reminderDialogVisible by remember { mutableStateOf(false) }
    var reminderInput by remember { mutableStateOf("") }
    var reminderInputError by remember { mutableStateOf(false) }
    var savedReminder by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    LaunchedEffect(state.waterIntakeMl) {
        if (state.waterIntakeMl < 2_000) {
            waterWasBelowGoal = true
        } else if (waterWasBelowGoal && !waterGoalCelebrated) {
            waterGoalCelebrated = true
            celebrationActive = true
            snackbarHostState.showSnackbar("Chúc mừng! Bạn đã hoàn thành mục tiêu uống nước.")
        }
    }
    val quickActions = listOf(
        QuickAction("+ Uống nước", Icons.Rounded.WaterDrop, Color(0xFF2563EB)) {
            onEvent(DashboardUiEvent.AddWater250Ml)
            scope.launch {
                snackbarHostState.showSnackbar("Đã thêm 250 ml nước vào nhật ký mẫu.")
            }
        },
        QuickAction("+ Ghi cân nặng", Icons.Rounded.MonitorWeight, Teal) {
            weightInputError = false
            weightDialogVisible = true
        },
        QuickAction("Đo nhịp tim", Icons.Rounded.FavoriteBorder, Color(0xFFE11D48)) {
            heartRateSheetVisible = true
        },
        QuickAction("Thêm nhắc nhở", Icons.Rounded.NotificationsActive, Color(0xFFD97706)) {
            reminderInputError = false
            reminderDialogVisible = true
        },
    )

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Header(
                    backendStatus = state.backendStatus,
                    onQuickAdd = { quickAddVisible = true },
                    onRetry = { onEvent(DashboardUiEvent.Retry) },
                )
            }
            item { AiInsightCard(onAskAi = { aiCoachVisible = true }) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard(
                            title = "Hoạt động",
                            value = formatVietnameseInteger(steps),
                            subtitle = "/ 8.000 bước",
                            icon = Icons.AutoMirrored.Rounded.DirectionsRun,
                            background = metricCardColor(Color(0xFFFFF1F2), Color(0xFF3B1020)),
                            accent = metricAccentColor(Color(0xFFE11D48), Color(0xFFFB7185)),
                            progress = (steps / 8_000f).coerceIn(0f, 1f),
                            modifier = Modifier.weight(1f),
                            animatedValue = steps,
                        ) { onNavigate(FoundationFeature.Activity) }
                        MetricCard("Tâm trạng", mood, null, Icons.Rounded.Mood, metricCardColor(Color(0xFFECFDF5), Color(0xFF103227)), metricAccentColor(Color(0xFF059669), Color(0xFF34D399)), null, Modifier.weight(1f), badge = "Ổn định") { onNavigate(FoundationFeature.Mood) }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard(
                            title = "Dinh dưỡng",
                            value = "1.450",
                            subtitle = "/ 2.000 kcal",
                            icon = Icons.Rounded.Restaurant,
                            background = metricCardColor(Color(0xFFEFF6FF), Color(0xFF112742)),
                            accent = metricAccentColor(Color(0xFF2563EB), Color(0xFF60A5FA)),
                            progress = .725f,
                            modifier = Modifier.weight(1f),
                            animatedValue = 1_450,
                        ) { onNavigate(FoundationFeature.Nutrition) }
                        SleepMetricCard(
                            sleepMinutes,
                            Modifier.weight(1f),
                            metricCardColor(Color(0xFFF5F3FF), Color(0xFF24193B)),
                            metricAccentColor(Color(0xFF7C3AED), Color(0xFFA78BFA)),
                        ) { onNavigate(FoundationFeature.Sleep) }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Truy cập nhanh", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text("Thao tác và danh mục sức khỏe", fontSize = 12.sp, color = Muted)
                    }
                    TextButton(onClick = { onNavigate(FoundationFeature.Analytics) }) { Text("Phân tích") }
                }
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 16.dp, top = 2.dp, bottom = 2.dp),
                ) {
                    items(
                        quickActions,
                        key = { it.label },
                    ) { action ->
                        QuickActionPill(action)
                    }
                }
            }
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        QuickAccessRow("Chỉ số cơ thể", "Cân nặng, chiều cao, BMI", Icons.Rounded.Straighten, Color(0xFF2563EB), Color(0xFFEFF6FF), "Xem") { onNavigate(FoundationFeature.BodyMetrics) }
                        DividerInset()
                        QuickAccessRow("Dấu hiệu sinh tồn", "Nhịp tim, huyết áp, SpO2", Icons.Rounded.FavoriteBorder, Color(0xFFE11D48), Color(0xFFFFF1F2), "Cập nhật") { onNavigate(FoundationFeature.Vitals) }
                        DividerInset()
                        QuickAccessRow("Hồ sơ y tế", "Đơn thuốc, lịch sử khám, dị ứng", Icons.Rounded.MedicalInformation, Color(0xFF0F766E), Color(0xFFCCFBF1), "Xem") { onNavigate(FoundationFeature.MedicalRecords) }
                        DividerInset()
                        QuickAccessRow("Health Connect", "Quản lý nguồn dữ liệu sức khỏe", Icons.Rounded.HealthAndSafety, Color(0xFF0F766E), Color(0xFFECFDF5), "Kết nối") { onNavigate(FoundationFeature.HealthConnect) }
                    }
                }
            }
            item {
                Text(
                    savedReminder?.let { "Nhắc nhở mẫu: $it" } ?: "Số liệu trên màn hình là dữ liệu mẫu để minh họa.",
                    Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    fontSize = 11.sp,
                    color = Muted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (celebrationActive) {
            ConfettiCelebration(active = true, onFinished = { celebrationActive = false })
        }
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp))
    }

    if (quickAddVisible) {
        ModalBottomSheet(onDismissRequest = { quickAddVisible = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Ghi nhanh sức khỏe", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text("Chọn loại dữ liệu muốn ghi nhận", color = Muted)
                listOf("Bước chân", "Ly nước", "Giấc ngủ", "Tâm trạng").forEach { option ->
                    Button(onClick = {
                        when (option) {
                            "Bước chân" -> {
                                val previousSteps = steps
                                steps = (steps + 500).coerceAtMost(12_000)
                                if (previousSteps < 8_000 && steps >= 8_000 && !stepGoalCelebrated) {
                                    stepGoalCelebrated = true
                                    celebrationActive = true
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Chúc mừng! Bạn đã hoàn thành mục tiêu bước chân.")
                                    }
                                }
                            }
                            "Ly nước" -> {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                                onEvent(DashboardUiEvent.AddWater250Ml)
                            }
                            "Giấc ngủ" -> sleepMinutes = (sleepMinutes + 30).coerceAtMost(12 * 60)
                            else -> mood = if (mood == "Tích cực") "Thư thái" else "Tích cực"
                        }
                        selectedQuickAdd = option
                        quickAddVisible = false
                    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if (option == "Ly nước") Teal else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (option == "Ly nước") MaterialTheme.colorScheme.onPrimary else Ink)) { Text(option) }
                }
            }
        }
    }
    selectedQuickAdd?.let { label ->
        AlertDialog(onDismissRequest = { selectedQuickAdd = null }, title = { Text("Đã cập nhật") }, text = { Text("Số liệu mẫu đã được cập nhật ngay trên giao diện.") }, confirmButton = { TextButton(onClick = { selectedQuickAdd = null }) { Text("Đóng") } })
    }

    if (weightDialogVisible) {
        AlertDialog(
            onDismissRequest = { weightDialogVisible = false },
            title = { Text("Ghi cân nặng") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nhập cân nặng hiện tại (kg).", color = Muted)
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it; weightInputError = false },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Cân nặng") },
                        suffix = { Text("kg") },
                        isError = weightInputError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    if (weightInputError) Text("Vui lòng nhập số cân nặng hợp lệ từ 20 đến 300 kg.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val parsedWeight = weightInput.replace(',', '.').toFloatOrNull()
                    if (parsedWeight != null && parsedWeight in 20f..300f) {
                        weightInput = "%.1f".format(Locale.US, parsedWeight)
                        weightDialogVisible = false
                        scope.launch { snackbarHostState.showSnackbar("Đã ghi nhận ${weightInput} kg vào dữ liệu mẫu.") }
                    } else {
                        weightInputError = true
                    }
                }) { Text("Lưu") }
            },
            dismissButton = { TextButton(onClick = { weightDialogVisible = false }) { Text("Hủy") } },
        )
    }

    if (reminderDialogVisible) {
        AlertDialog(
            onDismissRequest = { reminderDialogVisible = false },
            title = { Text("Thêm nhắc nhở") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tạo một nhắc nhở mẫu trên giao diện.", color = Muted)
                    OutlinedTextField(
                        value = reminderInput,
                        onValueChange = { reminderInput = it; reminderInputError = false },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Nội dung nhắc nhở") },
                        isError = reminderInputError,
                    )
                    if (reminderInputError) Text("Vui lòng nhập nội dung nhắc nhở.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val reminder = reminderInput.trim()
                    if (reminder.isNotEmpty()) {
                        savedReminder = reminder
                        reminderInput = ""
                        reminderDialogVisible = false
                        scope.launch { snackbarHostState.showSnackbar("Đã thêm nhắc nhở mẫu: $reminder") }
                    } else {
                        reminderInputError = true
                    }
                }) { Text("Tạo nhắc nhở") }
            },
            dismissButton = { TextButton(onClick = { reminderDialogVisible = false }) { Text("Hủy") } },
        )
    }

    if (heartRateSheetVisible) PulseSimulatorSheet(onDismiss = { heartRateSheetVisible = false })
    if (aiCoachVisible) HealthCoachSheet(onDismiss = { aiCoachVisible = false })
}

@Composable
private fun Header(
    backendStatus: BackendStatus,
    onQuickAdd: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.5.dp, Teal), modifier = Modifier.size(38.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Person, null, tint = Teal, modifier = Modifier.size(20.dp)) } }
            Text("Chào bạn,", Modifier.weight(1f), fontSize = 12.sp, color = Muted, maxLines = 1)
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Row(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.CalendarToday, null, tint = Teal, modifier = Modifier.size(15.dp))
                    Text(LocalDate.now().format(DateTimeFormatter.ofPattern("d 'thg' M", Locale.forLanguageTag("vi"))), fontSize = 11.sp, color = Ink, maxLines = 1)
                }
            }
            Surface(shape = CircleShape, color = Teal, modifier = Modifier.size(38.dp).bouncyClick(onClick = onQuickAdd)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Add, "Ghi nhanh", tint = MaterialTheme.colorScheme.onPrimary) } }
        }
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Sức khỏe mỗi ngày",
                Modifier.weight(1f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val connected = backendStatus == BackendStatus.CONNECTED
            val statusColor = if (connected) Color(0xFF059669) else Color(0xFFB45309)
            Surface(
                modifier = Modifier
                    .clickable(onClick = onRetry)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Chế độ Demo. " + when (backendStatus) {
                            BackendStatus.CONNECTED -> "Máy chủ đã kết nối."
                            BackendStatus.CHECKING -> "Đang kiểm tra kết nối máy chủ."
                            BackendStatus.UNAVAILABLE -> "Không thể kết nối máy chủ."
                        }
                    },
                shape = CircleShape,
                color = if (connected) Color(0xFFECFDF5) else Color(0xFFFFF7ED),
            ) {
                Row(
                    Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(statusColor))
                    Text(
                        "Chế độ Demo",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun AiInsightCard(onAskAi: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surfaceVariant))).padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface.copy(.85f), modifier = Modifier.size(34.dp)) { Box(contentAlignment = Alignment.Center) { PulsingIcon(Icons.Rounded.AutoAwesome, Teal) } }
                Text("Gợi ý từ HealthTrack AI", Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(.85f)) { Text("Dữ liệu mẫu", Modifier.padding(horizontal = 8.dp, vertical = 5.dp), color = Teal, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Bạn đã đạt 75% mục tiêu vận động. Hãy uống đủ nước và nghỉ ngơi đúng giờ nhé!", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 17.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            TextButton(onClick = onAskAi) { Text("Hỏi AI", maxLines = 1) }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String?,
    icon: ImageVector,
    background: Color,
    accent: Color,
    progress: Float?,
    modifier: Modifier,
    badge: String? = null,
    animatedValue: Int? = null,
    onClick: () -> Unit,
) {
    Card(modifier = modifier.height(138.dp).bouncyClick(onClick), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = background), border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = .16f))) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Surface(shape = RoundedCornerShape(10.dp), color = accent.copy(alpha = .12f), modifier = Modifier.size(29.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp)) } }
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (animatedValue != null) {
                AnimatedCounter(
                    value = animatedValue,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = if (value.length > 7) 18.sp else 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Text(value, fontSize = if (value.length > 7) 18.sp else 21.sp, fontWeight = FontWeight.Bold, color = accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (badge != null) {
                Surface(shape = CircleShape, color = accent.copy(alpha = .12f)) {
                    Text(badge, Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = accent, maxLines = 1)
                }
            }
            if (subtitle != null) Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (progress != null) {
                val animatedProgress by animateFromZeroFloatAsState(progress, label = "metric-progress-$title")
                LinearProgressIndicator(progress = { animatedProgress }, modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape), color = accent, trackColor = accent.copy(.14f))
            }
        }
    }
}

@Composable
private fun SleepMetricCard(sleepMinutes: Int, modifier: Modifier, background: Color, accent: Color, onClick: () -> Unit) {
    Card(modifier = modifier.height(138.dp).bouncyClick(onClick), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = background), border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(.16f))) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Surface(shape = RoundedCornerShape(10.dp), color = accent.copy(.12f), modifier = Modifier.size(29.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Bedtime, null, tint = accent, modifier = Modifier.size(18.dp)) } }
                Text("Giấc ngủ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
            Text("${sleepMinutes / 60}g ${sleepMinutes % 60}p", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = accent)
            Text("Chất lượng tốt · 85%", fontSize = 10.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(Modifier.fillMaxWidth().height(22.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
                listOf(11, 20, 15, 17).forEach { height -> Box(Modifier.weight(1f).height(height.dp).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(accent.copy(.65f))) }
            }
        }
    }
}

@Composable
private fun QuickAccessRow(title: String, subtitle: String, icon: ImageVector, accent: Color, pastel: Color, actionLabel: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = CircleShape, color = pastel, modifier = Modifier.size(38.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = accent, modifier = Modifier.size(21.dp)) } }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, fontSize = 11.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.clickable(onClick = onClick),
        ) {
            Text(actionLabel, Modifier.padding(horizontal = 11.dp, vertical = 7.dp), color = Teal, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun DividerInset() { HorizontalDivider(Modifier.padding(start = 62.dp), color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp) }

private data class QuickAction(
    val label: String,
    val icon: ImageVector,
    val accent: Color,
    val onClick: () -> Unit,
)

@Composable
private fun QuickActionPill(action: QuickAction) {
    val hapticFeedback = LocalHapticFeedback.current
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.bouncyClick {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
            action.onClick()
        },
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(action.icon, contentDescription = null, tint = action.accent, modifier = Modifier.size(17.dp))
            Text(action.label, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
private fun PulsingIcon(icon: ImageVector, tint: Color) {
    val transition = rememberInfiniteTransition(label = "health-icon-pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "health-icon-pulse-scale",
    )
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(20.dp).graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
    )
}


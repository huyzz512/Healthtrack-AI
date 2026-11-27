package com.healthtrackai.app.ui.screens.overview

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthtrackai.app.core.presentation.FoundationFeature
import com.healthtrackai.app.feature.dashboard.BackendStatus
import com.healthtrackai.app.feature.dashboard.DashboardUiEvent
import com.healthtrackai.app.feature.dashboard.DashboardUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private val Teal = Color(0xFF0F766E)
private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)

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
    var simulatedHeartRate by remember { mutableIntStateOf(72) }
    var reminderDialogVisible by remember { mutableStateOf(false) }
    var reminderInput by remember { mutableStateOf("") }
    var reminderInputError by remember { mutableStateOf(false) }
    var savedReminder by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Header(onQuickAdd = { quickAddVisible = true }) }
            item { AiInsightCard() }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard("Hoạt động", "%,d".format(steps), "/ 8,000 bước", Icons.AutoMirrored.Rounded.DirectionsRun, Color(0xFFFFF1F2), Color(0xFFE11D48), (steps / 8_000f).coerceIn(0f, 1f), Modifier.weight(1f)) { onNavigate(FoundationFeature.Activity) }
                        MetricCard("Tâm trạng", mood, null, Icons.Rounded.Mood, Color(0xFFECFDF5), Color(0xFF059669), null, Modifier.weight(1f), badge = "Ổn định") { onNavigate(FoundationFeature.Mood) }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard("Dinh dưỡng", "1,450", "/ 2,000 kcal", Icons.Rounded.Restaurant, Color(0xFFEFF6FF), Color(0xFF2563EB), .725f, Modifier.weight(1f)) { onNavigate(FoundationFeature.Nutrition) }
                        SleepMetricCard(sleepMinutes, Modifier.weight(1f)) { onNavigate(FoundationFeature.Sleep) }
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
                    contentPadding = PaddingValues(vertical = 2.dp),
                ) {
                    items(
                        listOf(
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
                        ),
                        key = { it.label },
                    ) { action ->
                        QuickActionPill(action)
                    }
                }
            }
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp))
    }

    if (quickAddVisible) {
        ModalBottomSheet(onDismissRequest = { quickAddVisible = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = Color.White) {
            Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Ghi nhanh sức khỏe", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text("Chọn loại dữ liệu muốn ghi nhận", color = Muted)
                listOf("Bước chân", "Ly nước", "Giấc ngủ", "Tâm trạng").forEach { option ->
                    Button(onClick = {
                        when (option) {
                            "Bước chân" -> steps = (steps + 500).coerceAtMost(12_000)
                            "Ly nước" -> onEvent(DashboardUiEvent.AddWater250Ml)
                            "Giấc ngủ" -> sleepMinutes = (sleepMinutes + 30).coerceAtMost(12 * 60)
                            else -> mood = if (mood == "Tích cực") "Thư thái" else "Tích cực"
                        }
                        selectedQuickAdd = option
                        quickAddVisible = false
                    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if (option == "Ly nước") Teal else Color(0xFFF1F5F9), contentColor = if (option == "Ly nước") Color.White else Ink)) { Text(option) }
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

    if (heartRateSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { heartRateSheetVisible = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
        ) {
            Column(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(shape = CircleShape, color = Color(0xFFFFF1F2), modifier = Modifier.size(56.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.FavoriteBorder, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(28.dp))
                    }
                }
                Text("Mô phỏng đo nhịp tim", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text("$simulatedHeartRate nhịp/phút", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE11D48))
                Text("Đây là số liệu minh họa, không lấy từ cảm biến hay thiết bị y tế.", color = Muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Button(
                    onClick = { simulatedHeartRate += 1 },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal),
                ) { Text("Mô phỏng đo lại") }
            }
        }
    }
}

@Composable
private fun Header(onQuickAdd: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Surface(shape = CircleShape, color = Color.White, border = androidx.compose.foundation.BorderStroke(1.5.dp, Teal), modifier = Modifier.size(38.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Person, null, tint = Teal, modifier = Modifier.size(20.dp)) } }
            Text("Chào bạn,", Modifier.weight(1f), fontSize = 12.sp, color = Muted, maxLines = 1)
            Surface(shape = CircleShape, color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                Row(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.CalendarToday, null, tint = Teal, modifier = Modifier.size(15.dp))
                    Text(LocalDate.now().format(DateTimeFormatter.ofPattern("d 'thg' M", Locale.forLanguageTag("vi"))), fontSize = 11.sp, color = Ink, maxLines = 1)
                }
            }
            Surface(shape = CircleShape, color = Teal, modifier = Modifier.size(38.dp).clickable(onClick = onQuickAdd)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Add, "Ghi nhanh", tint = Color.White) } }
        }
        Text("Sức khỏe mỗi ngày", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AiInsightCard() {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(listOf(Color(0xFFCCFBF1), Color(0xFFEFF6FF)))).padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(.85f), modifier = Modifier.size(34.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.AutoAwesome, null, tint = Teal) } }
                Text("Gợi ý từ HealthTrack AI", Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF134E4A), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Surface(shape = CircleShape, color = Color.White.copy(.85f)) { Text("Dữ liệu mẫu", Modifier.padding(horizontal = 8.dp, vertical = 5.dp), color = Teal, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
        }
        Text("Bạn đã đạt 75% mục tiêu vận động. Hãy uống đủ nước và nghỉ ngơi đúng giờ nhé!", color = Color(0xFF334155), fontSize = 12.sp, lineHeight = 17.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun MetricCard(title: String, value: String, subtitle: String?, icon: ImageVector, background: Color, accent: Color, progress: Float?, modifier: Modifier, badge: String? = null, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier.height(138.dp), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = background), border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = .16f))) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Surface(shape = RoundedCornerShape(10.dp), color = accent.copy(alpha = .12f), modifier = Modifier.size(29.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp)) } }
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(value, fontSize = if (value.length > 7) 18.sp else 21.sp, fontWeight = FontWeight.Bold, color = accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (badge != null) {
                Surface(shape = CircleShape, color = accent.copy(alpha = .12f)) {
                    Text(badge, Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = accent, maxLines = 1)
                }
            }
            if (subtitle != null) Text(subtitle, fontSize = 10.sp, color = Color(0xFF475569), maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (progress != null) LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape), color = accent, trackColor = accent.copy(.14f))
        }
    }
}

@Composable
private fun SleepMetricCard(sleepMinutes: Int, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier.height(138.dp), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C3AED).copy(.16f))) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF7C3AED).copy(.12f), modifier = Modifier.size(29.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Bedtime, null, tint = Color(0xFF7C3AED), modifier = Modifier.size(18.dp)) } }
                Text("Giấc ngủ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
            Text("${sleepMinutes / 60}g ${sleepMinutes % 60}p", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
            Text("Chất lượng tốt · 85%", fontSize = 10.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(Modifier.fillMaxWidth().height(22.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
                listOf(11, 20, 15, 17).forEach { height -> Box(Modifier.weight(1f).height(height.dp).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(Color(0xFF7C3AED).copy(.65f))) }
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
            color = Color(0xFFECFDF5),
            modifier = Modifier.clickable(onClick = onClick),
        ) {
            Text(actionLabel, Modifier.padding(horizontal = 11.dp, vertical = 7.dp), color = Teal, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun DividerInset() { HorizontalDivider(Modifier.padding(start = 62.dp), color = Color(0xFFF1F5F9), thickness = 1.dp) }

private data class QuickAction(
    val label: String,
    val icon: ImageVector,
    val accent: Color,
    val onClick: () -> Unit,
)

@Composable
private fun QuickActionPill(action: QuickAction) {
    Surface(
        shape = CircleShape,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.clickable(onClick = action.onClick),
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


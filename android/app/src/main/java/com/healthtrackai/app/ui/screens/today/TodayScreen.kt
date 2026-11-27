package com.healthtrackai.app.ui.screens.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthtrackai.app.model.MockTimelineEntry
import com.healthtrackai.app.model.mockTimelineEntries
import com.healthtrackai.app.ui.components.HealthScreenHeader
import java.time.LocalDate
import java.time.format.TextStyle
import java.text.NumberFormat
import java.util.Locale

private val Teal = Color(0xFF0F766E)
private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)

@Composable
fun TodayScreen(modifier: Modifier = Modifier) {
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }
    var waterMl by remember { mutableStateOf(1_500) }
    var showAddDialog by remember { mutableStateOf(false) }
    val entries = remember { mutableStateListOf<MockTimelineEntry>().apply { addAll(mockTimelineEntries) } }
    val dayValue = selectedDate.dayOfMonth
    val dailySteps = if (selectedDate == today) 6_420 else 4_200 + (dayValue * 173 % 3_800)
    val dailyCalories = if (selectedDate == today) 1_450 else 1_000 + (dayValue * 47 % 900)

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, top = 20.dp, end = 18.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                HealthScreenHeader("Hôm nay", "Nhật ký và tiến độ trong ngày")
            }
            item {
                WeekCalendarStrip(today = today, selectedDate = selectedDate, onSelect = { selectedDate = it })
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.forLanguageTag("vi"))).replaceFirstChar { it.uppercase() },
                        Modifier.weight(1f),
                        fontWeight = FontWeight.SemiBold,
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Surface(shape = CircleShape, color = Color(0xFFCCFBF1)) {
                        Text("DỮ LIỆU MẪU", Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = Teal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Mục tiêu hôm nay", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { (dailyCalories / 2_000f).coerceIn(0f, 1f) },
                                    modifier = Modifier.size(72.dp),
                                    color = Color(0xFF2563EB),
                                    trackColor = Color(0xFFDBEAFE),
                                    strokeWidth = 7.dp,
                                )
                                Text("${(dailyCalories * 100 / 2_000)}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("Năng lượng", fontWeight = FontWeight.SemiBold, color = Ink)
                                Text("${dailyCalories} / 2.000 kcal", fontSize = 12.sp, color = Muted)
                            }
                        }
                        GoalProgressRow("Số bước", "%,d / 8.000 bước".format(dailySteps), (dailySteps / 8_000f).coerceIn(0f, 1f), Color(0xFFE11D48))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Rounded.WaterDrop, null, tint = Color(0xFF2563EB), modifier = Modifier.size(22.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Nước uống", fontWeight = FontWeight.SemiBold, color = Ink)
                                Text("${NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(waterMl)} / 2.000 ml", fontSize = 12.sp, color = Muted)
                            }
                            TextButton(onClick = { waterMl = (waterMl + 250).coerceAtMost(2_000) }, enabled = waterMl < 2_000) { Text("+250ml") }
                        }
                        LinearProgressIndicator(progress = { waterMl / 2_000f }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = Color(0xFF2563EB), trackColor = Color(0xFFDBEAFE))
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Dòng thời gian", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text("${entries.size} hoạt động", fontSize = 12.sp, color = Muted)
                }
            }
            items(entries, key = { it.id }) { entry -> TimelineRow(entry) }
            item { Text("Các số liệu và bản ghi trên là dữ liệu giả lập để trình diễn giao diện.", fontSize = 12.sp, color = Muted, modifier = Modifier.padding(vertical = 4.dp)) }
        }
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 18.dp),
            containerColor = Teal,
            contentColor = Color.White,
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Ghi nhận nhanh")
            Text(" Ghi nhận", modifier = Modifier.padding(end = 14.dp), fontWeight = FontWeight.SemiBold)
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Thêm vào nhật ký") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Chọn một bản ghi mẫu để thêm vào hôm nay.", color = Muted)
                    AddRecordButton("Chạy bộ · 20 phút", Icons.AutoMirrored.Rounded.DirectionsRun) {
                        entries.add(MockTimelineEntry(System.currentTimeMillis(), "${java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}", "Chạy bộ", "20 phút · Bản ghi vừa thêm", "activity"))
                        showAddDialog = false
                    }
                    AddRecordButton("Bữa phụ · 180 kcal", Icons.Rounded.Restaurant) {
                        entries.add(MockTimelineEntry(System.currentTimeMillis(), "${java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}", "Bữa phụ", "180 kcal · Bản ghi vừa thêm", "nutrition"))
                        showAddDialog = false
                    }
                    AddRecordButton("Uống nước · 250 ml", Icons.Rounded.WaterDrop) {
                        waterMl = (waterMl + 250).coerceAtMost(2_000)
                        entries.add(MockTimelineEntry(System.currentTimeMillis(), "${java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}", "Uống nước", "250 ml · Bản ghi vừa thêm", "water"))
                        showAddDialog = false
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAddDialog = false }) { Text("Đóng") } },
        )
    }
}

@Composable
private fun WeekCalendarStrip(today: LocalDate, selectedDate: LocalDate, onSelect: (LocalDate) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        (0L..6L).map { today.minusDays(6L - it) }.forEach { date ->
            val selected = date == selectedDate
            Surface(
                modifier = Modifier.weight(1f).clickable { onSelect(date) },
                shape = RoundedCornerShape(18.dp),
                color = if (selected) Teal else Color.White,
                border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            ) {
                Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("vi")).take(2), fontSize = 10.sp, color = if (selected) Color.White.copy(.85f) else Muted)
                    Text(date.dayOfMonth.toString(), fontWeight = FontWeight.Bold, color = if (selected) Color.White else Ink)
                }
            }
        }
    }
}

@Composable
private fun GoalProgressRow(title: String, value: String, progress: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, fontWeight = FontWeight.SemiBold, color = Ink)
            Text(value, fontSize = 12.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = color, trackColor = color.copy(alpha = .12f))
    }
}

@Composable
private fun TimelineRow(entry: MockTimelineEntry) {
    val (icon, tint, background) = when (entry.iconKey) {
        "activity" -> Triple(Icons.AutoMirrored.Rounded.DirectionsRun, Color(0xFFE11D48), Color(0xFFFFF1F2))
        "nutrition" -> Triple(Icons.Rounded.Restaurant, Color(0xFF2563EB), Color(0xFFEFF6FF))
        else -> Triple(Icons.Rounded.WaterDrop, Color(0xFF0F766E), Color(0xFFCCFBF1))
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = background, modifier = Modifier.size(40.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp)) } }
            Spacer(Modifier.height(34.dp).padding(top = 2.dp).background(Color(0xFFE2E8F0)).size(width = 1.dp, height = 34.dp))
        }
        Card(Modifier.weight(1f), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(entry.title, fontWeight = FontWeight.SemiBold, color = Ink)
                    Text(entry.detail, fontSize = 12.sp, color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Text(entry.time, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Teal)
            }
        }
    }
}

@Composable
private fun AddRecordButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp)) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Text("  $label", maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

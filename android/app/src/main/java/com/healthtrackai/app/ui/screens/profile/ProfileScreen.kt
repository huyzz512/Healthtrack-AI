package com.healthtrackai.app.ui.screens.profile

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.MedicalInformation
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthtrackai.app.ui.components.HealthScreenHeader
import com.healthtrackai.app.core.presentation.FoundationFeature
import kotlinx.coroutines.launch

private val Teal = Color(0xFF0F766E)
private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)

@Composable
fun ProfileScreen(onNavigate: (FoundationFeature) -> Unit, modifier: Modifier = Modifier) {
    var waterRemindersEnabled by remember { mutableStateOf(true) }
    var heartRateAlertsEnabled by remember { mutableStateOf(true) }
    var demoModeEnabled by remember { mutableStateOf(true) }
    var stepGoal by remember { mutableStateOf(8_000) }
    var waterGoal by remember { mutableStateOf(2_000) }
    var editingGoal by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, top = 20.dp, end = 18.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                HealthScreenHeader("Hồ sơ", "Thông tin cá nhân và cài đặt")
            }
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            Surface(shape = CircleShape, color = Color(0xFFCCFBF1), modifier = Modifier.size(52.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Person, null, tint = Teal, modifier = Modifier.size(29.dp)) }
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("Nguyễn Minh Anh", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink, maxLines = 1)
                                Surface(shape = CircleShape, color = Color(0xFFECFDF5), modifier = Modifier.align(Alignment.Start)) {
                                    Text("Dữ liệu sức khỏe mẫu", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Teal, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                }
                                Text("28 tuổi · Nhóm máu O+", fontSize = 11.sp, color = Muted, maxLines = 1)
                            }
                            Button(
                                onClick = { onNavigate(FoundationFeature.Auth) },
                                modifier = Modifier.widthIn(max = 106.dp),
                                contentPadding = PaddingValues(horizontal = 7.dp, vertical = 7.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFECFDF5), contentColor = Teal),
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Icon(Icons.Rounded.Sync, contentDescription = null, modifier = Modifier.size(17.dp))
                                    Text("Đăng nhập /\nĐồng bộ", fontSize = 9.sp, lineHeight = 11.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }
                        androidx.compose.material3.HorizontalDivider(color = Color(0xFFE2E8F0))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            ProfileMetricColumn("172 cm", "Chiều cao", Modifier.weight(1f))
                            Box(Modifier.width(1.dp).height(38.dp).background(Color(0xFFE2E8F0)))
                            ProfileMetricColumn("68 kg", "Cân nặng", Modifier.weight(1f))
                            Box(Modifier.width(1.dp).height(38.dp).background(Color(0xFFE2E8F0)))
                            ProfileMetricColumn("23,0", "BMI · Tốt", Modifier.weight(1f))
                        }
                    }
                }
            }
            item { SectionTitle("Quản lý sức khỏe", "Xem và cập nhật hồ sơ của bạn") }
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        ProfileAccessRow("Hồ sơ sức khỏe", "Thông tin cá nhân và chỉ số cơ thể", Icons.Rounded.HealthAndSafety) {
                            onNavigate(FoundationFeature.HealthProfile)
                        }
                        androidx.compose.material3.HorizontalDivider(Modifier.padding(start = 62.dp), color = Color(0xFFF1F5F9))
                        ProfileAccessRow("Hồ sơ y tế", "Đơn thuốc, lần khám và dị ứng", Icons.Rounded.MedicalInformation) {
                            onNavigate(FoundationFeature.MedicalRecords)
                        }
                    }
                }
            }
            item { SectionTitle("Mục tiêu cá nhân", "Chạm vào mục tiêu để điều chỉnh") }
            item {
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        GoalSettingRow("Mục tiêu bước chân", "${stepGoal} bước / ngày", Icons.Rounded.Tune) { editingGoal = "steps" }
                        androidx.compose.material3.HorizontalDivider(color = Color(0xFFF1F5F9))
                        GoalSettingRow("Mục tiêu uống nước", "${waterGoal} ml / ngày", Icons.Rounded.Tune) { editingGoal = "water" }
                    }
                }
            }
            item { SectionTitle("Cài đặt ứng dụng", "Bạn có thể đổi các lựa chọn bất cứ lúc nào") }
            item {
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        SwitchSettingRow("Nhắc nhở uống nước", "Nhận nhắc uống nước trong ngày", Icons.Rounded.NotificationsActive, waterRemindersEnabled) { waterRemindersEnabled = it }
                        androidx.compose.material3.HorizontalDivider(color = Color(0xFFF1F5F9))
                        SwitchSettingRow("Cảnh báo nhịp tim", "Hiển thị cảnh báo nhịp tim mẫu", Icons.Rounded.FavoriteBorder, heartRateAlertsEnabled) { heartRateAlertsEnabled = it }
                        androidx.compose.material3.HorizontalDivider(color = Color(0xFFF1F5F9))
                        SwitchSettingRow("Chế độ dữ liệu mẫu", "Dùng số liệu minh họa trên thiết bị", Icons.Rounded.Person, demoModeEnabled) { demoModeEnabled = it }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5))) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(12.dp), color = Color.White, modifier = Modifier.size(42.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Assessment, null, tint = Teal) } }
                        Column(Modifier.weight(1f)) {
                            Text("Xuất báo cáo PDF", fontWeight = FontWeight.Bold, color = Ink)
                            Text("Xem trước báo cáo từ dữ liệu mẫu", fontSize = 12.sp, color = Muted)
                        }
                        Button(
                            onClick = { scope.launch { snackbarHostState.showSnackbar("Bản xem trước báo cáo PDF mẫu đã sẵn sàng.") } },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) { Text("Xuất PDF", fontSize = 11.sp, maxLines = 1) }
                    }
                }
            }
            item { Text("Thông tin trong hồ sơ hiện là dữ liệu giả lập, chưa được đồng bộ với máy chủ.", Modifier.fillMaxWidth().padding(vertical = 4.dp), fontSize = 12.sp, color = Muted, textAlign = TextAlign.Center) }
        }
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp))
    }

    editingGoal?.let { goal ->
        val initial = if (goal == "steps") stepGoal.toFloat() else waterGoal.toFloat()
        var sliderValue by remember(goal) { mutableStateOf(initial) }
        val isSteps = goal == "steps"
        val range = if (isSteps) 2_000f..20_000f else 500f..4_000f
        AlertDialog(
            onDismissRequest = { editingGoal = null },
            title = { Text(if (isSteps) "Mục tiêu bước chân" else "Mục tiêu uống nước") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (isSteps) "${sliderValue.toInt()} bước mỗi ngày" else "${sliderValue.toInt()} ml mỗi ngày", color = Teal, fontWeight = FontWeight.Bold)
                    Slider(value = sliderValue, onValueChange = { sliderValue = it }, valueRange = range, steps = if (isSteps) 17 else 13)
                    Text("Bạn có thể thay đổi mục tiêu này sau.", color = Muted, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (isSteps) stepGoal = (sliderValue.toInt() / 500) * 500 else waterGoal = (sliderValue.toInt() / 250) * 250
                    editingGoal = null
                }) { Text("Lưu mục tiêu") }
            },
            dismissButton = { TextButton(onClick = { editingGoal = null }) { Text("Hủy") } },
        )
    }

}

@Composable
private fun ProfileMetricColumn(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = 5.dp, vertical = 3.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(value, fontSize = 15.sp, color = Teal, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(label, fontSize = 10.sp, color = Muted, maxLines = 1)
    }
}

@Composable
private fun ProfileAccessRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = CircleShape, color = Color(0xFFECFDF5), modifier = Modifier.size(38.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Teal, modifier = Modifier.size(20.dp))
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = Ink, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text(subtitle, fontSize = 11.sp, color = Muted, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFECFDF5),
            modifier = Modifier.clickable(onClick = onClick),
        ) {
            Text("Xem", Modifier.padding(horizontal = 13.dp, vertical = 8.dp), color = Teal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text(subtitle, fontSize = 12.sp, color = Muted)
    }
}

@Composable
private fun GoalSettingRow(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(11.dp), color = Color(0xFFCCFBF1), modifier = Modifier.size(38.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = Teal, modifier = Modifier.size(19.dp)) } }
        Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold, color = Ink); Text(value, fontSize = 12.sp, color = Muted) }
        Text("›", fontSize = 24.sp, color = Muted)
    }
}

@Composable
private fun SwitchSettingRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(11.dp), color = Color(0xFFF1F5F9), modifier = Modifier.size(38.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = Teal, modifier = Modifier.size(19.dp)) } }
        Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold, color = Ink); Text(subtitle, fontSize = 11.sp, color = Muted) }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

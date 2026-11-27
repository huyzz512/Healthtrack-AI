package com.healthtrackai.app.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.MedicalInformation
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthtrackai.app.core.presentation.FoundationFeature

private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)
private val Teal = Color(0xFF0F766E)

private data class DetailMetric(val label: String, val value: String, val note: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDetailScreen(feature: FoundationFeature?, onBack: () -> Unit) {
    val title = feature?.title ?: "Chi tiết sức khỏe"
    val (icon, accent, tint, summary, metrics) = detailContent(feature)

    Scaffold(
        containerColor = Color(0xFFF7F9FC),
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF7F9FC)),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 18.dp, top = 10.dp, end = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = tint),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(18.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(shape = RoundedCornerShape(14.dp), color = Color.White) {
                            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.padding(10.dp))
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Text(summary, fontSize = 13.sp, color = Color(0xFF334155), lineHeight = 19.sp)
                        }
                    }
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Thông tin gần đây", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Surface(shape = CircleShape, color = Color(0xFFCCFBF1)) {
                        Text("Dữ liệu mẫu", Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = Teal, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            items(metrics) { metric ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(metric.label, fontSize = 13.sp, color = Muted)
                        Text(metric.value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = accent)
                        Text(metric.note, fontSize = 12.sp, color = Muted, lineHeight = 17.sp)
                    }
                }
            }
            item {
                Text(
                    "Các thông tin trên chỉ nhằm minh họa giao diện và không thay thế tư vấn y tế.",
                    fontSize = 12.sp,
                    color = Muted,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

private fun detailContent(
    feature: FoundationFeature?,
): DetailContent = when (feature) {
    FoundationFeature.Activity -> DetailContent(
        Icons.AutoMirrored.Rounded.DirectionsRun,
        Color(0xFFE11D48),
        Color(0xFFFFF1F2),
        "Theo dõi vận động và tiến độ bước chân trong ngày.",
        listOf(
            DetailMetric("Số bước hôm nay", "6.420 bước", "Đạt 80% mục tiêu 8.000 bước."),
            DetailMetric("Thời gian vận động", "42 phút", "Bao gồm đi bộ và chạy bộ."),
            DetailMetric("Quãng đường", "4,8 km", "Ước tính từ dữ liệu mẫu."),
        ),
    )
    FoundationFeature.Mood -> DetailContent(
        Icons.Rounded.Assessment,
        Color(0xFF059669),
        Color(0xFFECFDF5),
        "Ghi nhận cảm xúc và quan sát thay đổi tâm trạng.",
        listOf(
            DetailMetric("Tâm trạng gần nhất", "Tích cực", "Được ghi nhận lúc 10:30."),
            DetailMetric("Xu hướng tuần", "Ổn định", "Phần lớn bản ghi ở trạng thái tích cực."),
            DetailMetric("Gợi ý", "Dành thời gian nghỉ ngắn", "Một khoảng thư giãn có thể giúp bạn cân bằng."),
        ),
    )
    FoundationFeature.Nutrition -> DetailContent(
        Icons.Rounded.Restaurant,
        Color(0xFF2563EB),
        Color(0xFFEFF6FF),
        "Tổng hợp năng lượng và các bản ghi dinh dưỡng trong ngày.",
        listOf(
            DetailMetric("Năng lượng đã dùng", "1.450 kcal", "Mục tiêu mẫu: 2.000 kcal."),
            DetailMetric("Bữa ăn", "3 bữa", "Bữa sáng, bữa trưa và bữa phụ."),
            DetailMetric("Nước uống", "1.500 ml", "Mục tiêu mẫu: 2.000 ml."),
        ),
    )
    FoundationFeature.Reminders -> DetailContent(
        Icons.Rounded.NotificationsActive,
        Color(0xFFD97706),
        Color(0xFFFFF7ED),
        "Các nhắc nhở sức khỏe được hiển thị dưới dạng dữ liệu mẫu.",
        listOf(
            DetailMetric("Uống nước", "Mỗi 2 giờ", "Lần nhắc tiếp theo: 14:00."),
            DetailMetric("Uống thuốc", "20:00 hôm nay", "Nhắc dùng theo lịch minh họa."),
            DetailMetric("Vận động", "Sau 45 phút ngồi", "Đứng dậy đi lại nhẹ nhàng."),
        ),
    )
    FoundationFeature.BodyMetrics -> DetailContent(
        Icons.Rounded.Straighten,
        Color(0xFF2563EB),
        Color(0xFFEFF6FF),
        "Theo dõi các chỉ số cơ thể được cung cấp trong hồ sơ mẫu.",
        listOf(
            DetailMetric("Chiều cao", "172 cm", "Cập nhật trong hồ sơ mẫu."),
            DetailMetric("Cân nặng", "68 kg", "Số đo minh họa gần nhất."),
            DetailMetric("Chỉ số BMI", "23,0 · Bình thường", "Chỉ số tham khảo, không phải chẩn đoán."),
        ),
    )
    FoundationFeature.Vitals -> DetailContent(
        Icons.Rounded.FavoriteBorder,
        Color(0xFFE11D48),
        Color(0xFFFFF1F2),
        "Một số chỉ số sinh tồn minh họa để trình bày giao diện.",
        listOf(
            DetailMetric("Nhịp tim nghỉ", "72 nhịp/phút", "Bản ghi mẫu lúc 08:00."),
            DetailMetric("Huyết áp", "118/76 mmHg", "Bản ghi mẫu gần nhất."),
            DetailMetric("SpO2", "98%", "Số liệu giả lập, không phải kết quả đo."),
        ),
    )
    FoundationFeature.MedicalRecords -> DetailContent(
        Icons.Rounded.MedicalInformation,
        Teal,
        Color(0xFFCCFBF1),
        "Tóm tắt hồ sơ y tế mẫu được hiển thị trên thiết bị.",
        listOf(
            DetailMetric("Lịch sử khám", "2 hồ sơ", "Các bản ghi minh họa."),
            DetailMetric("Đơn thuốc", "1 đơn mẫu", "Không dùng thay cho chỉ định điều trị."),
            DetailMetric("Dị ứng", "Chưa ghi nhận", "Thông tin mẫu chưa được xác minh."),
        ),
    )
    FoundationFeature.HealthProfile -> DetailContent(
        Icons.Rounded.HealthAndSafety,
        Teal,
        Color(0xFFECFDF5),
        "Tổng quan hồ sơ sức khỏe cá nhân trong giao diện dữ liệu mẫu.",
        listOf(
            DetailMetric("Thông tin cá nhân", "Nguyễn Minh Anh", "28 tuổi · Nhóm máu O+."),
            DetailMetric("Chiều cao và cân nặng", "172 cm · 68 kg", "Số đo minh họa trong hồ sơ."),
            DetailMetric("BMI", "23,0 · Tốt", "Chỉ số tham khảo, không phải chẩn đoán."),
        ),
    )
    FoundationFeature.HealthConnect -> DetailContent(
        Icons.Rounded.HealthAndSafety,
        Teal,
        Color(0xFFECFDF5),
        "Trang minh họa quản lý nguồn dữ liệu sức khỏe trên thiết bị.",
        listOf(
            DetailMetric("Trạng thái kết nối", "Chưa kết nối", "Giao diện mẫu không truy cập Health Connect."),
            DetailMetric("Quyền dữ liệu", "Chưa cấp quyền", "Không có dữ liệu thiết bị nào được đọc."),
            DetailMetric("Nguồn dữ liệu", "Dữ liệu mẫu", "Kết nối thật chưa được bật trong bản giao diện này."),
        ),
    )
    FoundationFeature.Sleep -> DetailContent(
        Icons.Rounded.Bedtime,
        Color(0xFF7C3AED),
        Color(0xFFF5F3FF),
        "Tóm tắt thời lượng nghỉ ngơi và chất lượng giấc ngủ gần đây.",
        listOf(
            DetailMetric("Thời lượng ngủ", "7g 30p", "Đạt gần mục tiêu nghỉ ngơi mẫu."),
            DetailMetric("Chất lượng", "Tốt · 85%", "Chỉ số minh họa, không phải đánh giá y tế."),
            DetailMetric("Khung giờ", "23:00 – 06:30", "Thời gian ngủ mẫu của đêm gần nhất."),
        ),
    )
    else -> DetailContent(
        Icons.Rounded.Bedtime,
        Teal,
        Color(0xFFCCFBF1),
        "Chi tiết dữ liệu sức khỏe mẫu.",
        listOf(DetailMetric("Trạng thái", "Sẵn sàng", "Đây là nội dung minh họa.")),
    )
}

private data class DetailContent(
    val icon: ImageVector,
    val accent: Color,
    val tint: Color,
    val summary: String,
    val metrics: List<DetailMetric>,
)

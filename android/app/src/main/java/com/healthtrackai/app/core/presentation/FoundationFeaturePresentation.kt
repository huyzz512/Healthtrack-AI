package com.healthtrackai.app.core.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthtrackai.app.core.common.LoadableState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

data class FoundationContent(val title: String, val summary: String)
typealias FoundationUiState = LoadableState<FoundationContent>

sealed interface FoundationUiEvent {
    data object Load : FoundationUiEvent
    data object Retry : FoundationUiEvent
    data object DismissError : FoundationUiEvent
    data class RequestUnavailableAction(val action: String) : FoundationUiEvent
}

sealed interface FoundationUiEffect {
    data class ShowMessage(val message: String) : FoundationUiEffect
}

open class FoundationFeatureViewModel(private val feature: FoundationFeature) : ViewModel() {
    private val _uiState = MutableStateFlow<FoundationUiState>(feature.contentState())
    val uiState: StateFlow<FoundationUiState> = _uiState.asStateFlow()
    private val effectChannel = Channel<FoundationUiEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    fun onEvent(event: FoundationUiEvent) {
        when (event) {
            FoundationUiEvent.Load, FoundationUiEvent.Retry -> {
                _uiState.value = feature.contentState()
                effectChannel.trySend(FoundationUiEffect.ShowMessage("Đã làm mới trạng thái giao diện."))
            }
            FoundationUiEvent.DismissError -> _uiState.value = LoadableState.Empty
            is FoundationUiEvent.RequestUnavailableAction -> effectChannel.trySend(
                FoundationUiEffect.ShowMessage("${event.action}: API nghiệp vụ chưa được kết nối, dữ liệu chưa được gửi hoặc lưu."),
            )
        }
    }
}

private fun FoundationFeature.contentState(): FoundationUiState =
    LoadableState.Content(FoundationContent(title = title, summary = summary))

@Composable
fun FoundationFeatureRoute(
    viewModel: FoundationFeatureViewModel,
    screen: @Composable (FoundationUiState, (FoundationUiEvent) -> Unit) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is FoundationUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(modifier = Modifier.padding(padding)) { screen(state, viewModel::onEvent) }
    }
}

private data class ScreenSpec(
    val eyebrow: String,
    val intro: String,
    val fields: List<Pair<String, String>> = emptyList(),
    val sections: List<Pair<String, String>> = emptyList(),
    val action: String = "Lưu thay đổi",
    val emptyTitle: String = "Chưa có dữ liệu",
    val emptyBody: String = "Dữ liệu sẽ xuất hiện tại đây khi chức năng được kết nối.",
)

private fun FoundationFeature.screenSpec(): ScreenSpec = when (this) {
    FoundationFeature.HealthProfile -> ScreenSpec(
        "HỒ SƠ CỦA BẠN", "Thông tin nền giúp cá nhân hóa trải nghiệm theo dõi sức khỏe.",
        listOf("Chiều cao" to "cm", "Nhóm máu" to "Chọn nhóm máu", "Dị ứng" to "Thêm thông tin dị ứng", "Bệnh nền" to "Thêm tình trạng sức khỏe"),
        listOf("Thông tin cá nhân" to "Ngày sinh và giới tính sẽ được lấy theo hồ sơ tài khoản đã xác nhận."),
        "Lưu hồ sơ",
    )
    FoundationFeature.BodyMetrics -> ScreenSpec(
        "THEO DÕI CƠ THỂ", "Ghi nhận số đo theo thời gian. Đơn vị và thời điểm được xác nhận trước khi đồng bộ.",
        listOf("Cân nặng" to "kg", "Chiều cao" to "cm", "Vòng eo" to "cm", "Ngày ghi nhận" to "dd/mm/yyyy"),
        listOf("Lịch sử chỉ số" to "Biểu đồ sẽ hiển thị sau khi có dữ liệu thật."), "Thêm chỉ số",
    )
    FoundationFeature.Vitals -> ScreenSpec(
        "CHỈ SỐ SỨC KHỎE", "Ghi nhận các dấu hiệu sinh tồn theo hướng dẫn chuyên môn của bạn.",
        listOf("Huyết áp tâm thu" to "mmHg", "Huyết áp tâm trương" to "mmHg", "Nhịp tim" to "lần/phút", "Nhiệt độ" to "°C", "SpO₂" to "%", "Thời điểm đo" to "dd/mm/yyyy · hh:mm"),
        listOf("Lịch sử đo" to "Chưa có bản ghi để tổng hợp."), "Lưu kết quả đo",
    )
    FoundationFeature.MedicalRecords -> ScreenSpec(
        "HỒ SƠ Y TẾ", "Quản lý tài liệu sức khỏe riêng tư của bạn.",
        listOf("Tên tài liệu" to "Ví dụ: Kết quả xét nghiệm", "Loại hồ sơ" to "Chọn loại tài liệu", "Ngày khám" to "dd/mm/yyyy", "Ghi chú" to "Thông tin thêm"),
        listOf("Tệp đính kèm" to "Chọn PDF, JPG hoặc PNG. Tệp y tế cần được lưu ở chế độ riêng tư."), "Chọn tệp",
        "Chưa có hồ sơ y tế", "Tài liệu đã tải lên sẽ được liệt kê tại đây.",
    )
    FoundationFeature.Reminders -> ScreenSpec(
        "NHẮC LỊCH CỦA BẠN", "Tạo nhắc nhở và quản lý thời gian thông báo trên thiết bị.",
        listOf("Tên nhắc nhở" to "Ví dụ: Uống thuốc", "Loại nhắc" to "Thuốc · uống nước · giấc ngủ", "Giờ nhắc" to "hh:mm", "Lặp lại" to "Hằng ngày · tùy chỉnh"),
        listOf("Thông báo trên thiết bị" to "Cần quyền thông báo để nhận nhắc lịch. Trạng thái lưu và trạng thái thông báo sẽ được hiển thị riêng."), "Tạo nhắc lịch",
    )
    FoundationFeature.Nutrition -> ScreenSpec(
        "DINH DƯỠNG HẰNG NGÀY", "Ghi nhận bữa ăn và lượng nước theo mục tiêu do bạn thiết lập.",
        listOf("Tên món / thức uống" to "Tìm hoặc nhập tên", "Bữa ăn" to "Sáng · trưa · tối · phụ", "Năng lượng" to "kcal", "Lượng nước" to "ml", "Thời điểm" to "hh:mm"),
        listOf("Nhật ký hôm nay" to "Tổng quan sẽ được tính từ các bản ghi thực tế."), "Thêm ghi nhận",
    )
    FoundationFeature.Activity -> ScreenSpec(
        "VẬN ĐỘNG", "Xem hoạt động đã đồng bộ hoặc ghi nhận theo nguồn dữ liệu được xác nhận.",
        sections = listOf("Hoạt động gần đây" to "Chưa có hoạt động được đồng bộ.", "Nguồn dữ liệu" to "Bản ghi sẽ được gắn nguồn: nhập tay hoặc Health Connect."),
        action = "Kết nối nguồn dữ liệu", emptyTitle = "Chưa có hoạt động", emptyBody = "Kết nối Health Connect hoặc thêm hoạt động khi luồng được hỗ trợ.",
    )
    FoundationFeature.Analytics -> ScreenSpec(
        "XU HƯỚNG SỨC KHỎE", "Theo dõi sự thay đổi của các chỉ số theo khoảng thời gian.",
        sections = listOf("Khoảng thời gian" to "7 ngày · 30 ngày · tùy chỉnh", "Biểu đồ xu hướng" to "Biểu đồ cần dữ liệu đã đồng bộ và đơn vị chuẩn."),
        action = "Làm mới thống kê", emptyTitle = "Chưa đủ dữ liệu", emptyBody = "Thêm hoặc đồng bộ dữ liệu sức khỏe để xem xu hướng.",
    )
    FoundationFeature.Reports -> ScreenSpec(
        "BÁO CÁO SỨC KHỎE", "Tạo bản tổng hợp để xem lại hoặc chia sẻ theo quyền truy cập được kiểm soát.",
        listOf("Khoảng thời gian" to "Chọn ngày bắt đầu và kết thúc", "Nhóm dữ liệu" to "Chọn chỉ số cần đưa vào"),
        listOf("Báo cáo đã tạo" to "Báo cáo PDF sẽ xuất hiện sau khi dịch vụ báo cáo được kết nối."), "Tạo báo cáo",
        "Chưa có báo cáo", "Bạn có thể xem các báo cáo đã tạo tại đây.",
    )
    FoundationFeature.AiAssistant -> ScreenSpec(
        "TRỢ LÝ SỨC KHỎE", "Đặt câu hỏi thông tin chung. Trợ lý không thay thế tư vấn y tế.",
        listOf("Câu hỏi của bạn" to "Nhập câu hỏi tại đây"),
        listOf("Quyền riêng tư" to "Dữ liệu sức khỏe chỉ được gửi khi có consent phù hợp và được tối thiểu hóa.", "Lịch sử trao đổi" to "Chưa có cuộc trò chuyện."),
        "Gửi câu hỏi", "Chưa có trao đổi", "Nội dung sẽ xuất hiện sau khi dịch vụ được kết nối.",
    )
    else -> ScreenSpec("HEALTHTRACK AI", summary)
}

@Composable
fun FoundationFeatureScreen(feature: FoundationFeature, state: FoundationUiState, onEvent: (FoundationUiEvent) -> Unit) {
    when (state) {
        LoadableState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        LoadableState.Empty -> EmptyFeatureScreen(feature, "Chưa có dữ liệu để hiển thị.", onEvent)
        is LoadableState.Error -> EmptyFeatureScreen(feature, state.message, onEvent)
        is LoadableState.Offline -> EmptyFeatureScreen(feature, state.cachedValue?.summary ?: "Đang ngoại tuyến. Kiểm tra kết nối rồi thử lại.", onEvent)
        is LoadableState.Unavailable -> EmptyFeatureScreen(feature, state.reason, onEvent)
        is LoadableState.Content -> FeatureContent(feature, onEvent)
    }
}

@Composable
private fun EmptyFeatureScreen(feature: FoundationFeature, message: String, onEvent: (FoundationUiEvent) -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(feature.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = { onEvent(FoundationUiEvent.Retry) }) { Text("Thử lại") }
    }
}

@Composable
private fun FeatureContent(feature: FoundationFeature, onEvent: (FoundationUiEvent) -> Unit) {
    val spec = feature.screenSpec()
    val values = remember(feature) { mutableStateMapOf<String, String>() }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(spec.eyebrow, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(feature.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(spec.intro, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Không bịa dữ liệu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Các chỉ số, lịch sử và biểu đồ chỉ hiển thị sau khi có dữ liệu thật.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        if (spec.fields.isNotEmpty()) {
            Text("Thông tin", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            spec.fields.forEach { (label, hint) ->
                var value by remember(feature, label) { mutableStateOf("") }
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it; values[label] = it },
                    label = { Text(label) },
                    placeholder = { Text(hint) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = label != "Ghi chú" && label != "Câu hỏi của bạn",
                    minLines = if (label == "Ghi chú" || label == "Câu hỏi của bạn") 3 else 1,
                )
            }
        }
        spec.sections.forEach { (title, body) ->
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (title.contains("Lịch sử") || title.contains("Báo cáo") || title.contains("hôm nay") || title.contains("gần đây") || title.contains("trao đổi")) {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text(spec.emptyTitle, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        Button(
            onClick = { onEvent(FoundationUiEvent.RequestUnavailableAction(spec.action)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) { Text(spec.action) }
        TextButton(onClick = { onEvent(FoundationUiEvent.Retry) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Làm mới giao diện")
        }
        Text("API nghiệp vụ chưa kết nối · nội dung chưa được lưu", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

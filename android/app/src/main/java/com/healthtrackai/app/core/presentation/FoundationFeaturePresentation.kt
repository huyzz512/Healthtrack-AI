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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.platform.LocalContext
import com.healthtrackai.app.core.common.LoadableState
import com.healthtrackai.app.core.domain.LocalHealthEntry
import com.healthtrackai.app.core.domain.LocalHealthEntryRepository
import com.healthtrackai.app.core.di.healthTrackEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

data class FoundationContent(
    val title: String,
    val summary: String,
    val entries: List<LocalHealthEntry> = emptyList(),
)
typealias FoundationUiState = LoadableState<FoundationContent>

sealed interface FoundationUiEvent {
    data object Load : FoundationUiEvent
    data object Retry : FoundationUiEvent
    data object DismissError : FoundationUiEvent
    data class RequestUnavailableAction(val action: String) : FoundationUiEvent
    data class AddLocalEntry(val title: String, val content: String) : FoundationUiEvent
    data class UpdateLocalEntry(val id: String, val title: String, val content: String) : FoundationUiEvent
    data class DeleteLocalEntry(val id: String) : FoundationUiEvent
}

sealed interface FoundationUiEffect {
    data class ShowMessage(val message: String) : FoundationUiEffect
}

open class FoundationFeatureViewModel(
    private val feature: FoundationFeature,
    private val repository: LocalHealthEntryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<FoundationUiState>(feature.contentState())
    val uiState: StateFlow<FoundationUiState> = _uiState.asStateFlow()
    private val effectChannel = Channel<FoundationUiEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.observe(feature.route).collectLatest { entries ->
                val current = _uiState.value
                if (current is LoadableState.Content) {
                    _uiState.value = LoadableState.Content(current.value.copy(entries = entries))
                }
            }
        }
    }

    fun onEvent(event: FoundationUiEvent) {
        when (event) {
            FoundationUiEvent.Load, FoundationUiEvent.Retry -> {
                _uiState.value = feature.contentState()
                effectChannel.trySend(FoundationUiEffect.ShowMessage("Đã làm mới trạng thái giao diện."))
            }
            FoundationUiEvent.DismissError -> _uiState.value = LoadableState.Empty
            is FoundationUiEvent.AddLocalEntry -> viewModelScope.launch {
                val isUpdatingProfile = feature == FoundationFeature.HealthProfile &&
                    (_uiState.value as? LoadableState.Content)?.value?.entries?.isNotEmpty() == true
                runCatching {
                    val currentEntries = (_uiState.value as? LoadableState.Content)?.value?.entries.orEmpty()
                    val existingProfile = currentEntries.firstOrNull().takeIf { feature == FoundationFeature.HealthProfile }
                    if (existingProfile == null) repository.add(feature.route, event.title, event.content)
                    else repository.update(existingProfile.copy(content = event.content, recordedAtEpochMillis = System.currentTimeMillis()))
                }.onSuccess {
                    val message = if (isUpdatingProfile) {
                        "Đã cập nhật hồ sơ trên thiết bị này. Dữ liệu chưa được đồng bộ lên máy chủ."
                    } else {
                        "Đã lưu trên thiết bị này. Dữ liệu chưa được đồng bộ lên máy chủ."
                    }
                    effectChannel.send(FoundationUiEffect.ShowMessage(message))
                }.onFailure {
                    effectChannel.send(FoundationUiEffect.ShowMessage("Không lưu được dữ liệu trên thiết bị. Vui lòng thử lại."))
                }
            }
            is FoundationUiEvent.UpdateLocalEntry -> viewModelScope.launch {
                val current = (_uiState.value as? LoadableState.Content)?.value?.entries?.firstOrNull { it.id == event.id }
                if (current != null) {
                    runCatching {
                        repository.update(current.copy(
                            title = event.title,
                            content = event.content,
                            recordedAtEpochMillis = System.currentTimeMillis(),
                        ))
                    }.onSuccess {
                        effectChannel.send(FoundationUiEffect.ShowMessage("Đã cập nhật bản ghi trên thiết bị này."))
                    }.onFailure {
                        effectChannel.send(FoundationUiEffect.ShowMessage("Không cập nhật được bản ghi. Vui lòng thử lại."))
                    }
                }
            }
            is FoundationUiEvent.DeleteLocalEntry -> viewModelScope.launch {
                runCatching { repository.delete(event.id) }
                    .onSuccess { effectChannel.send(FoundationUiEffect.ShowMessage("Đã xóa bản ghi khỏi thiết bị này.")) }
                    .onFailure { effectChannel.send(FoundationUiEffect.ShowMessage("Không xóa được bản ghi. Vui lòng thử lại.")) }
            }
            is FoundationUiEvent.RequestUnavailableAction -> effectChannel.trySend(
                FoundationUiEffect.ShowMessage("${event.action}: API nghiệp vụ chưa được kết nối, dữ liệu chưa được gửi hoặc lưu."),
            )
        }
    }
}

@Composable
fun <T : FoundationFeatureViewModel> foundationFeatureViewModel(
    key: String,
    modelClass: KClass<T>,
    create: (LocalHealthEntryRepository) -> T,
): T = viewModelWithLocalHealthRepository(key, modelClass, create)

@Composable
fun <T : ViewModel> viewModelWithLocalHealthRepository(
    key: String,
    modelClass: KClass<T>,
    create: (LocalHealthEntryRepository) -> T,
): T {
    val context = LocalContext.current.applicationContext
    val repository = remember(context) { healthTrackEntryPoint(context).localHealthEntryRepository() }
    val viewModelCreator = create
    val factory = remember(repository, key) {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = viewModelCreator(repository) as VM
        }
    }
    return viewModel(modelClass = modelClass, key = key, factory = factory)
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
    val localSaveEnabled: Boolean = false,
)

private fun FoundationFeature.screenSpec(): ScreenSpec = when (this) {
    FoundationFeature.HealthProfile -> ScreenSpec(
        "HỒ SƠ CỦA BẠN", "Thông tin nền giúp cá nhân hóa trải nghiệm theo dõi sức khỏe.",
        listOf("Chiều cao (cm)" to "Nhập chiều cao", "Nhóm máu" to "Nhập nhóm máu", "Dị ứng" to "Thêm thông tin dị ứng", "Bệnh nền" to "Thêm tình trạng sức khỏe"),
        listOf("Thông tin cá nhân" to "Ngày sinh và giới tính sẽ được lấy theo hồ sơ tài khoản đã xác nhận."),
        "Lưu hồ sơ",
        localSaveEnabled = true,
    )
    FoundationFeature.BodyMetrics -> ScreenSpec(
        "THEO DÕI CƠ THỂ", "Ghi nhận số đo theo thời gian. Đơn vị và thời điểm được xác nhận trước khi đồng bộ.",
        listOf("Cân nặng (kg)" to "Nhập số kg", "Chiều cao (cm)" to "Nhập số cm", "Vòng eo (cm)" to "Nhập số cm", "Ngày ghi nhận" to "dd/mm/yyyy"),
        listOf("Lịch sử chỉ số" to "Biểu đồ sẽ hiển thị sau khi có dữ liệu thật."), "Thêm chỉ số",
        localSaveEnabled = true,
    )
    FoundationFeature.Vitals -> ScreenSpec(
        "CHỈ SỐ SỨC KHỎE", "Ghi nhận các dấu hiệu sinh tồn theo hướng dẫn chuyên môn của bạn.",
        listOf("Huyết áp tâm thu (mmHg)" to "Nhập số mmHg", "Huyết áp tâm trương (mmHg)" to "Nhập số mmHg", "Nhịp tim (bpm)" to "Nhập nhịp/phút", "Nhiệt độ (°C)" to "Nhập nhiệt độ", "SpO₂ (%)" to "Nhập phần trăm", "Thời điểm đo" to "dd/mm/yyyy · hh:mm"),
        listOf("Lịch sử đo" to "Chưa có bản ghi để tổng hợp."), "Lưu kết quả đo",
        localSaveEnabled = true,
    )
    FoundationFeature.MedicalRecords -> ScreenSpec(
        "HỒ SƠ Y TẾ", "Quản lý tài liệu sức khỏe riêng tư của bạn.",
        listOf("Tên tài liệu" to "Ví dụ: Kết quả xét nghiệm", "Loại hồ sơ" to "Chọn loại tài liệu", "Ngày khám" to "dd/mm/yyyy", "Ghi chú" to "Thông tin thêm"),
        listOf("Tệp đính kèm" to "Chọn PDF, JPG hoặc PNG. Tệp y tế cần được lưu ở chế độ riêng tư."), "Chọn tệp",
        "Chưa có hồ sơ y tế", "Tài liệu đã tải lên sẽ được liệt kê tại đây.",
        localSaveEnabled = false,
    )
    FoundationFeature.Reminders -> ScreenSpec(
        "NHẮC LỊCH CỦA BẠN", "Tạo nhắc nhở và quản lý thời gian thông báo trên thiết bị.",
        listOf("Tên nhắc nhở" to "Ví dụ: Uống thuốc", "Loại nhắc" to "Thuốc · uống nước · giấc ngủ", "Giờ nhắc" to "hh:mm", "Lặp lại" to "Hằng ngày · tùy chỉnh"),
        listOf("Thông báo trên thiết bị" to "Cần quyền thông báo để nhận nhắc lịch. Trạng thái lưu và trạng thái thông báo sẽ được hiển thị riêng."), "Tạo nhắc lịch",
        localSaveEnabled = false,
    )
    FoundationFeature.Nutrition -> ScreenSpec(
        "DINH DƯỠNG HẰNG NGÀY", "Ghi nhận bữa ăn và lượng nước theo mục tiêu do bạn thiết lập.",
        listOf("Tên món / thức uống" to "Nhập tên", "Bữa ăn" to "Sáng · trưa · tối · phụ", "Năng lượng (kcal)" to "Nhập kcal", "Lượng nước (ml)" to "Nhập ml", "Thời điểm" to "hh:mm"),
        listOf("Nhật ký hôm nay" to "Tổng quan sẽ được tính từ các bản ghi thực tế."), "Thêm ghi nhận",
        localSaveEnabled = true,
    )
    FoundationFeature.Activity -> ScreenSpec(
        "VẬN ĐỘNG", "Xem hoạt động đã đồng bộ hoặc ghi nhận theo nguồn dữ liệu được xác nhận.",
        fields = listOf("Loại hoạt động" to "Đi bộ · chạy · đạp xe", "Thời lượng (phút)" to "Nhập số phút", "Số bước" to "Nhập số bước", "Ngày hoạt động" to "dd/mm/yyyy"),
        sections = listOf("Hoạt động gần đây" to "Chưa có hoạt động được đồng bộ.", "Nguồn dữ liệu" to "Bản ghi sẽ được gắn nguồn: nhập tay hoặc Health Connect."),
        action = "Lưu hoạt động trên thiết bị", emptyTitle = "Chưa có hoạt động", emptyBody = "Kết nối Health Connect hoặc thêm hoạt động khi luồng được hỗ trợ.",
        localSaveEnabled = true,
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
        is LoadableState.Content -> FeatureContent(feature, state.value.entries, onEvent)
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
private fun FeatureContent(
    feature: FoundationFeature,
    entries: List<LocalHealthEntry>,
    onEvent: (FoundationUiEvent) -> Unit,
) {
    val spec = feature.screenSpec()
    val values = remember(feature) { mutableStateMapOf<String, String>() }
    var editingId by remember(feature) { mutableStateOf<String?>(null) }
    var deleteTarget by remember(feature) { mutableStateOf<String?>(null) }
    var validationMessage by remember(feature) { mutableStateOf<String?>(null) }
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
                OutlinedTextField(
                    value = values[label] ?: "",
                    onValueChange = { values[label] = it; validationMessage = null },
                    label = { Text(label) },
                    placeholder = { Text(hint) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = label != "Ghi chú" && label != "Câu hỏi của bạn",
                    minLines = if (label == "Ghi chú" || label == "Câu hỏi của bạn") 3 else 1,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (label.contains("kg") || label.contains("cm") || label.contains("mmHg") ||
                            label.contains("bpm") || label.contains("°C") || label.contains("kcal") || label.contains("ml") ||
                            label.contains("SpO₂") || label.contains("Số bước")
                        ) KeyboardType.Decimal else KeyboardType.Text,
                    ),
                )
            }
        }
        if (entries.isNotEmpty()) {
            Text("Đã lưu trên thiết bị này", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            entries.forEach { entry ->
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(entry.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(entry.content, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT)
                                .format(java.util.Date(entry.recordedAtEpochMillis)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                editingId = entry.id
                                values.clear()
                                entry.content.lines().forEach { line ->
                                    val separator = line.indexOf(':')
                                    if (separator > 0) values[line.substring(0, separator).trim()] = line.substring(separator + 1).trim()
                                }
                            }) { Text("Sửa") }
                            TextButton(onClick = { deleteTarget = entry.id }) { Text("Xóa") }
                        }
                    }
                }
            }
        }
        spec.sections.forEach { (title, body) ->
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (entries.isEmpty() && (title.contains("Lịch sử") || title.contains("Báo cáo") || title.contains("hôm nay") || title.contains("gần đây") || title.contains("trao đổi"))) {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text(spec.emptyTitle, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        validationMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
        Button(
            onClick = {
                val content = values.entries
                    .filter { it.value.isNotBlank() }
                    .joinToString(separator = "\n") { (label, value) -> "$label: ${value.trim()}" }
                if (spec.localSaveEnabled && content.isNotBlank()) {
                    val invalidNumber = values.entries.firstOrNull { (label, value) ->
                        value.isNotBlank() && (label.contains("kg") || label.contains("cm") || label.contains("mmHg") ||
                            label.contains("bpm") || label.contains("°C") || label.contains("kcal") || label.contains("ml") ||
                            label.contains("SpO₂") || label.contains("Số bước")) &&
                            (value.toDoubleOrNull() == null || value.toDoubleOrNull()!! <= 0.0)
                    }
                    if (invalidNumber != null) {
                        validationMessage = "${invalidNumber.key}: vui lòng nhập số lớn hơn 0."
                        return@Button
                    }
                    val currentEditingId = editingId
                    if (currentEditingId == null) {
                        onEvent(FoundationUiEvent.AddLocalEntry(feature.title, content))
                    } else {
                        onEvent(FoundationUiEvent.UpdateLocalEntry(currentEditingId, feature.title, content))
                    }
                    editingId = null
                    values.clear()
                    validationMessage = null
                } else {
                    onEvent(FoundationUiEvent.RequestUnavailableAction(spec.action))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) { Text(if (editingId == null) spec.action else "Cập nhật bản ghi") }
        TextButton(onClick = { onEvent(FoundationUiEvent.Retry) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Làm mới giao diện")
        }
        Text(
            if (spec.localSaveEnabled) "Bản ghi được lưu cục bộ trên thiết bị; chưa đồng bộ với Backend."
            else "Chức năng này cần API nghiệp vụ từ Backend để hoạt động.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Xóa bản ghi?") },
            text = { Text("Bản ghi sẽ bị xóa khỏi thiết bị này.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteTarget?.let { onEvent(FoundationUiEvent.DeleteLocalEntry(it)) }
                    deleteTarget = null
                }) { Text("Xóa") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Hủy") } },
        )
    }
}

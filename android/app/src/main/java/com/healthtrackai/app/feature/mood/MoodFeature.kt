package com.healthtrackai.app.feature.mood

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthtrackai.app.core.network.contract.MoodType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class MoodPage {
    ENTRY,
    HISTORY,
}

data class MoodUiState(
    val page: MoodPage = MoodPage.ENTRY,
    val selectedMood: MoodType? = null,
    val note: String = "",
    val validationMessage: String? = null,
    val statusMessage: String? = null,
)

sealed interface MoodUiEvent {
    data class SelectMood(val mood: MoodType) : MoodUiEvent
    data class NoteChanged(val value: String) : MoodUiEvent
    data object ShowEntry : MoodUiEvent
    data object ShowHistory : MoodUiEvent
    data object Save : MoodUiEvent
}

class MoodViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MoodUiState())
    val uiState: StateFlow<MoodUiState> = _uiState.asStateFlow()

    fun onEvent(event: MoodUiEvent) {
        val current = _uiState.value
        _uiState.value = when (event) {
            is MoodUiEvent.SelectMood -> current.copy(
                selectedMood = event.mood,
                validationMessage = null,
                statusMessage = null,
            )

            is MoodUiEvent.NoteChanged -> current.copy(
                note = event.value,
                statusMessage = null,
            )

            MoodUiEvent.ShowEntry -> current.copy(page = MoodPage.ENTRY)
            MoodUiEvent.ShowHistory -> current.copy(page = MoodPage.HISTORY)
            MoodUiEvent.Save -> if (current.selectedMood == null) {
                current.copy(
                    validationMessage = "Vui lòng chọn trạng thái cảm xúc.",
                    statusMessage = null,
                )
            } else {
                current.copy(
                    validationMessage = null,
                    statusMessage = "API nhật ký cảm xúc chưa được kết nối; dữ liệu chưa được lưu.",
                )
            }
        }
    }
}

private data class MoodOption(
    val type: MoodType,
    val label: String,
    val emoji: String,
)

private val moodOptions = listOf(
    MoodOption(MoodType.HAPPY, "Vui vẻ", "😊"),
    MoodOption(MoodType.NORMAL, "Bình thường", "😌"),
    MoodOption(MoodType.SAD, "Buồn bã", "😔"),
    MoodOption(MoodType.STRESSED, "Căng thẳng", "😣"),
    MoodOption(MoodType.ANXIOUS, "Lo lắng", "😟"),
)

@Composable
fun MoodRoute(viewModel: MoodViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MoodScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun MoodScreen(
    state: MoodUiState,
    onEvent: (MoodUiEvent) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            MoodBottomBar(
                state = state,
                onEvent = onEvent,
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MoodHeader(page = state.page)
            MoodTabs(
                page = state.page,
                onEntry = { onEvent(MoodUiEvent.ShowEntry) },
                onHistory = { onEvent(MoodUiEvent.ShowHistory) },
            )

            when (state.page) {
                MoodPage.ENTRY -> MoodEntryContent(state = state, onEvent = onEvent)
                MoodPage.HISTORY -> MoodHistoryContent(onEntry = { onEvent(MoodUiEvent.ShowEntry) })
            }
        }
    }
}

@Composable
private fun MoodHeader(page: MoodPage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "NHẬT KÝ CẢM XÚC",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (page == MoodPage.ENTRY) "Hôm nay bạn thế nào?" else "Nhìn lại cảm xúc",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Ghi nhận để hiểu mình hơn mỗi ngày.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(
                text = "✦",
                fontSize = 36.sp,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun MoodTabs(
    page: MoodPage,
    onEntry: () -> Unit,
    onHistory: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            MoodTab(
                label = "Ghi nhận",
                selected = page == MoodPage.ENTRY,
                onClick = onEntry,
                modifier = Modifier.weight(1f),
            )
            MoodTab(
                label = "Lịch sử",
                selected = page == MoodPage.HISTORY,
                onClick = onHistory,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MoodTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun MoodEntryContent(
    state: MoodUiState,
    onEvent: (MoodUiEvent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Chọn cảm xúc phù hợp nhất",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Bạn có thể ghi chú thêm, nhưng không bắt buộc.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        MoodOptions(
            selectedMood = state.selectedMood,
            onSelect = { onEvent(MoodUiEvent.SelectMood(it)) },
        )

        state.validationMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Ghi chú",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = state.note,
                onValueChange = { onEvent(MoodUiEvent.NoteChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Điều gì đang ở trong suy nghĩ của bạn?") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(18.dp),
                supportingText = { Text("Tùy chọn") },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("◷", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(
                text = "Thời điểm ghi nhận được xác định khi lưu.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Text(
                text = "Nhật ký này giúp bạn theo dõi cảm xúc cá nhân, không dùng để chẩn đoán.",
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MoodOptions(
    selectedMood: MoodType?,
    onSelect: (MoodType) -> Unit,
) {
    val rows = moodOptions.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowOptions.forEach { option ->
                    MoodChoiceCard(
                        option = option,
                        selected = selectedMood == option.type,
                        onClick = { onSelect(option.type) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowOptions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MoodChoiceCard(
    option: MoodOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 76.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(option.emoji, fontSize = 24.sp)
                if (selected) {
                    Surface(
                        modifier = Modifier.size(22.dp),
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
            Text(
                text = option.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

@Composable
private fun MoodHistoryContent(onEntry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("🌱", fontSize = 42.sp)
            Text(
                text = "Nhật ký của bạn sẽ bắt đầu từ đây",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Chưa có bản ghi. API lịch sử chưa được kết nối nên ứng dụng chưa thể tải dữ liệu.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            TextButton(onClick = onEntry) {
                Text("Ghi nhận cảm xúc")
            }
        }
    }
}

@Composable
private fun MoodBottomBar(
    state: MoodUiState,
    onEvent: (MoodUiEvent) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.page == MoodPage.ENTRY) {
                state.statusMessage?.let { message ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }
            Button(
                onClick = {
                    if (state.page == MoodPage.ENTRY) {
                        onEvent(MoodUiEvent.Save)
                    } else {
                        onEvent(MoodUiEvent.ShowEntry)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(
                    text = if (state.page == MoodPage.ENTRY) "Lưu cảm xúc" else "Ghi nhận cảm xúc",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

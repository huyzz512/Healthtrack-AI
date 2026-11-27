package com.healthtrackai.app.feature.healthconnect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthtrackai.app.core.common.LoadableState
import com.healthtrackai.app.core.di.healthTrackEntryPoint
import com.healthtrackai.app.core.healthconnect.domain.HealthConnectAvailability
import com.healthtrackai.app.core.healthconnect.domain.HealthConnectGateway
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

typealias HealthConnectUiState = LoadableState<HealthConnectAvailability>

sealed interface HealthConnectUiEvent {
    data object RefreshAvailability : HealthConnectUiEvent
}

sealed interface HealthConnectUiEffect {
    data class ShowMessage(val message: String) : HealthConnectUiEffect
}

class HealthConnectViewModel(
    private val gateway: HealthConnectGateway,
) : ViewModel() {
    private val _uiState = MutableStateFlow<HealthConnectUiState>(LoadableState.Loading)
    val uiState: StateFlow<HealthConnectUiState> = _uiState.asStateFlow()

    private val effectChannel = Channel<HealthConnectUiEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    init {
        refreshAvailability(showConfirmation = false)
    }

    fun onEvent(event: HealthConnectUiEvent) {
        when (event) {
            HealthConnectUiEvent.RefreshAvailability -> refreshAvailability(showConfirmation = true)
        }
    }

    private fun refreshAvailability(showConfirmation: Boolean) {
        _uiState.value = runCatching { LoadableState.Content(gateway.availability()) }
            .getOrElse { LoadableState.Error("Không kiểm tra được khả năng sử dụng Health Connect.") }
        if (showConfirmation) {
            effectChannel.trySend(HealthConnectUiEffect.ShowMessage("Đã kiểm tra lại trạng thái Health Connect."))
        }
    }
}

@Composable
fun HealthConnectRoute() {
    val context = LocalContext.current.applicationContext
    val gateway = remember(context) { healthTrackEntryPoint(context).healthConnectGateway() }
    val factory = remember(gateway) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = HealthConnectViewModel(gateway) as T
        }
    }
    val viewModel: HealthConnectViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HealthConnectUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        HealthConnectScreen(
            state = state,
            onEvent = viewModel::onEvent,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun HealthConnectScreen(
    state: HealthConnectUiState,
    onEvent: (HealthConnectUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("KẾT NỐI SỨC KHỎE", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text("Health Connect", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Chọn nguồn dữ liệu sức khỏe trên thiết bị và kiểm soát quyền truy cập của bạn.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        when (state) {
            LoadableState.Loading -> CircularProgressIndicator()
            LoadableState.Empty -> Text("Chưa có trạng thái Health Connect.")
            is LoadableState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is LoadableState.Offline -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is LoadableState.Unavailable -> Text(state.reason, color = MaterialTheme.colorScheme.error)
            is LoadableState.Content -> Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Trạng thái trên thiết bị", style = MaterialTheme.typography.titleMedium)
                    HealthConnectAvailabilityContent(state.value)
                }
            }
        }
        Text(
            "Hiện tại ứng dụng chỉ kiểm tra khả năng hỗ trợ. Chưa xin quyền và chưa đọc dữ liệu sức khỏe.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = { onEvent(HealthConnectUiEvent.RefreshAvailability) }) {
            Text("Kiểm tra lại")
        }
    }
}

@Composable
private fun HealthConnectAvailabilityContent(availability: HealthConnectAvailability) {
    val message = when (availability) {
        HealthConnectAvailability.Available -> "Thiết bị đã sẵn sàng hỗ trợ Health Connect."
        HealthConnectAvailability.ProviderUpdateRequired -> "Cần cập nhật nhà cung cấp Health Connect trên thiết bị."
        HealthConnectAvailability.Unavailable -> "Thiết bị hiện chưa hỗ trợ Health Connect."
        is HealthConnectAvailability.Unknown -> "Trạng thái SDK chưa được nhận diện: ${availability.sdkStatus}."
    }
    Text(message)
}

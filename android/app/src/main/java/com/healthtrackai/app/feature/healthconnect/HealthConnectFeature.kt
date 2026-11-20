package com.healthtrackai.app.feature.healthconnect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthtrackai.app.core.common.LoadableState
import com.healthtrackai.app.core.healthconnect.domain.HealthConnectAvailability
import com.healthtrackai.app.core.healthconnect.domain.HealthConnectGateway
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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

@HiltViewModel
class HealthConnectViewModel @Inject constructor(
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
            .getOrElse { LoadableState.Error("Health Connect availability could not be checked.") }
        if (showConfirmation) {
            effectChannel.trySend(HealthConnectUiEffect.ShowMessage("Availability refreshed"))
        }
    }
}

@Composable
fun HealthConnectRoute(viewModel: HealthConnectViewModel) {
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
        Text("Health Connect", style = MaterialTheme.typography.headlineMedium)
        when (state) {
            LoadableState.Loading -> CircularProgressIndicator()
            LoadableState.Empty -> Text("No Health Connect state is available.")
            is LoadableState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is LoadableState.Offline -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is LoadableState.Unavailable -> Text(state.reason, color = MaterialTheme.colorScheme.error)
            is LoadableState.Content -> HealthConnectAvailabilityContent(state.value)
        }
        Text("No permissions are requested and no health records are read in Foundation.")
        Button(onClick = { onEvent(HealthConnectUiEvent.RefreshAvailability) }) {
            Text("Refresh availability")
        }
    }
}

@Composable
private fun HealthConnectAvailabilityContent(availability: HealthConnectAvailability) {
    val message = when (availability) {
        HealthConnectAvailability.Available -> "Health Connect is available."
        HealthConnectAvailability.ProviderUpdateRequired -> "Health Connect requires a provider update."
        HealthConnectAvailability.Unavailable -> "Health Connect is unavailable on this device."
        is HealthConnectAvailability.Unknown -> "Unknown Health Connect SDK status: ${availability.sdkStatus}."
    }
    Text(message)
}

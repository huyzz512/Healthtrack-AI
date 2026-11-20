package com.healthtrackai.app.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthtrackai.app.core.presentation.FoundationFeature
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

data class DashboardUiState(
    val isLoading: Boolean = false,
    val destinations: List<FoundationFeature> = FoundationFeature.dashboardDestinations,
    val errorMessage: String? = null,
)

sealed interface DashboardUiEvent {
    data class SelectFeature(val feature: FoundationFeature) : DashboardUiEvent
    data object Retry : DashboardUiEvent
}

sealed interface DashboardUiEffect {
    data class NavigateToFeature(val feature: FoundationFeature) : DashboardUiEffect
    data class ShowMessage(val message: String) : DashboardUiEffect
}

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val effectChannel = Channel<DashboardUiEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            is DashboardUiEvent.SelectFeature -> {
                effectChannel.trySend(DashboardUiEffect.NavigateToFeature(event.feature))
            }

            DashboardUiEvent.Retry -> {
                _uiState.value = DashboardUiState()
                effectChannel.trySend(DashboardUiEffect.ShowMessage("Foundation destinations refreshed"))
            }
        }
    }
}

@Composable
fun DashboardRoute(
    onNavigate: (FoundationFeature) -> Unit,
    viewModel: DashboardViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel, onNavigate) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DashboardUiEffect.NavigateToFeature -> onNavigate(effect.feature)
                is DashboardUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        DashboardScreen(
            state = state,
            onEvent = viewModel::onEvent,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("HealthTrack AI", style = MaterialTheme.typography.headlineMedium)
        Text("Foundation v0.1 — architecture placeholders; UC-01 through UC-20 are not implemented.")
        when {
            state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            state.errorMessage != null -> {
                Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
                Button(onClick = { onEvent(DashboardUiEvent.Retry) }) { Text("Retry") }
            }

            state.destinations.isEmpty() -> Text("No Foundation destinations are configured.")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.destinations, key = FoundationFeature::route) { feature ->
                    Button(
                        onClick = { onEvent(DashboardUiEvent.SelectFeature(feature)) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(feature.title)
                    }
                }
            }
        }
    }
}

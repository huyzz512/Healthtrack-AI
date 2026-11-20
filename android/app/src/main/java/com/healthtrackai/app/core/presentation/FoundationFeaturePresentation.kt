package com.healthtrackai.app.core.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthtrackai.app.core.common.LoadableState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

data class FoundationContent(
    val title: String,
    val summary: String,
)

typealias FoundationUiState = LoadableState<FoundationContent>

sealed interface FoundationUiEvent {
    data object Load : FoundationUiEvent
    data object Retry : FoundationUiEvent
    data object DismissError : FoundationUiEvent
}

sealed interface FoundationUiEffect {
    data class ShowMessage(val message: String) : FoundationUiEffect
}

open class FoundationFeatureViewModel(
    private val feature: FoundationFeature,
) : ViewModel() {
    private val _uiState = MutableStateFlow<FoundationUiState>(feature.contentState())
    val uiState: StateFlow<FoundationUiState> = _uiState.asStateFlow()

    private val effectChannel = Channel<FoundationUiEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    fun onEvent(event: FoundationUiEvent) {
        when (event) {
            FoundationUiEvent.Load,
            FoundationUiEvent.Retry,
            -> {
                _uiState.value = feature.contentState()
                effectChannel.trySend(FoundationUiEffect.ShowMessage("Foundation placeholder refreshed"))
            }

            FoundationUiEvent.DismissError -> _uiState.value = LoadableState.Empty
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
        Box(modifier = Modifier.padding(padding)) {
            screen(state, viewModel::onEvent)
        }
    }
}

@Composable
fun FoundationFeatureScreen(
    feature: FoundationFeature,
    state: FoundationUiState,
    onEvent: (FoundationUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(feature.title, style = MaterialTheme.typography.headlineMedium)
        when (state) {
            LoadableState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            LoadableState.Empty -> FoundationMessage("No data is available yet.", onEvent)
            is LoadableState.Error -> FoundationMessage(state.message, onEvent)
            is LoadableState.Offline -> {
                Text(state.cachedValue?.summary ?: state.message)
                Text("Offline", color = MaterialTheme.colorScheme.error)
                Button(onClick = { onEvent(FoundationUiEvent.Retry) }) { Text("Retry") }
            }

            is LoadableState.Unavailable -> FoundationMessage(state.reason, onEvent)
            is LoadableState.Content -> {
                Text(state.value.summary, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Foundation placeholder only. Business flows are intentionally not implemented.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun FoundationMessage(
    message: String,
    onEvent: (FoundationUiEvent) -> Unit,
) {
    Text(message)
    Button(onClick = { onEvent(FoundationUiEvent.Retry) }) { Text("Retry") }
}

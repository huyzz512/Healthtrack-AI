package com.healthtrackai.app.feature.aiassistant

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthtrackai.app.core.presentation.FoundationFeature
import com.healthtrackai.app.core.presentation.FoundationFeatureRoute
import com.healthtrackai.app.core.presentation.FoundationFeatureScreen
import com.healthtrackai.app.core.presentation.FoundationFeatureViewModel
import com.healthtrackai.app.core.presentation.FoundationUiEffect
import com.healthtrackai.app.core.presentation.FoundationUiEvent
import com.healthtrackai.app.core.presentation.FoundationUiState

typealias AiAssistantUiState = FoundationUiState
typealias AiAssistantUiEvent = FoundationUiEvent
typealias AiAssistantUiEffect = FoundationUiEffect

class AiAssistantViewModel : FoundationFeatureViewModel(FoundationFeature.AiAssistant)

@Composable
fun AiAssistantRoute(viewModel: AiAssistantViewModel = viewModel()) {
    FoundationFeatureRoute(viewModel) { state, onEvent -> AiAssistantScreen(state, onEvent) }
}

@Composable
fun AiAssistantScreen(state: AiAssistantUiState, onEvent: (AiAssistantUiEvent) -> Unit) {
    FoundationFeatureScreen(FoundationFeature.AiAssistant, state, onEvent)
}

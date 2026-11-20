package com.healthtrackai.app.feature.reminders

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthtrackai.app.core.presentation.FoundationFeature
import com.healthtrackai.app.core.presentation.FoundationFeatureRoute
import com.healthtrackai.app.core.presentation.FoundationFeatureScreen
import com.healthtrackai.app.core.presentation.FoundationFeatureViewModel
import com.healthtrackai.app.core.presentation.FoundationUiEffect
import com.healthtrackai.app.core.presentation.FoundationUiEvent
import com.healthtrackai.app.core.presentation.FoundationUiState

typealias RemindersUiState = FoundationUiState
typealias RemindersUiEvent = FoundationUiEvent
typealias RemindersUiEffect = FoundationUiEffect

class RemindersViewModel : FoundationFeatureViewModel(FoundationFeature.Reminders)

@Composable
fun RemindersRoute(viewModel: RemindersViewModel = viewModel()) {
    FoundationFeatureRoute(viewModel) { state, onEvent -> RemindersScreen(state, onEvent) }
}

@Composable
fun RemindersScreen(state: RemindersUiState, onEvent: (RemindersUiEvent) -> Unit) {
    FoundationFeatureScreen(FoundationFeature.Reminders, state, onEvent)
}

package com.healthtrackai.app.feature.mood

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthtrackai.app.core.presentation.FoundationFeature
import com.healthtrackai.app.core.presentation.FoundationFeatureRoute
import com.healthtrackai.app.core.presentation.FoundationFeatureScreen
import com.healthtrackai.app.core.presentation.FoundationFeatureViewModel
import com.healthtrackai.app.core.presentation.FoundationUiEffect
import com.healthtrackai.app.core.presentation.FoundationUiEvent
import com.healthtrackai.app.core.presentation.FoundationUiState

typealias MoodUiState = FoundationUiState
typealias MoodUiEvent = FoundationUiEvent
typealias MoodUiEffect = FoundationUiEffect

class MoodViewModel : FoundationFeatureViewModel(FoundationFeature.Mood)

@Composable
fun MoodRoute(viewModel: MoodViewModel = viewModel()) {
    FoundationFeatureRoute(viewModel) { state, onEvent -> MoodScreen(state, onEvent) }
}

@Composable
fun MoodScreen(state: MoodUiState, onEvent: (MoodUiEvent) -> Unit) {
    FoundationFeatureScreen(FoundationFeature.Mood, state, onEvent)
}

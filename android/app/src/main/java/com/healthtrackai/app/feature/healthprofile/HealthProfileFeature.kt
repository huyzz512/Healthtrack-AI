package com.healthtrackai.app.feature.healthprofile

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthtrackai.app.core.presentation.FoundationFeature
import com.healthtrackai.app.core.presentation.FoundationFeatureRoute
import com.healthtrackai.app.core.presentation.FoundationFeatureScreen
import com.healthtrackai.app.core.presentation.FoundationFeatureViewModel
import com.healthtrackai.app.core.presentation.FoundationUiEffect
import com.healthtrackai.app.core.presentation.FoundationUiEvent
import com.healthtrackai.app.core.presentation.FoundationUiState

typealias HealthProfileUiState = FoundationUiState
typealias HealthProfileUiEvent = FoundationUiEvent
typealias HealthProfileUiEffect = FoundationUiEffect

class HealthProfileViewModel : FoundationFeatureViewModel(FoundationFeature.HealthProfile)

@Composable
fun HealthProfileRoute(viewModel: HealthProfileViewModel = viewModel()) {
    FoundationFeatureRoute(viewModel) { state, onEvent -> HealthProfileScreen(state, onEvent) }
}

@Composable
fun HealthProfileScreen(state: HealthProfileUiState, onEvent: (HealthProfileUiEvent) -> Unit) {
    FoundationFeatureScreen(FoundationFeature.HealthProfile, state, onEvent)
}

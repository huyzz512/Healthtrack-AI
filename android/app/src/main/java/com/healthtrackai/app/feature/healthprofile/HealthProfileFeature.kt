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
import com.healthtrackai.app.core.presentation.foundationFeatureViewModel
import com.healthtrackai.app.core.domain.LocalHealthEntryRepository

typealias HealthProfileUiState = FoundationUiState
typealias HealthProfileUiEvent = FoundationUiEvent
typealias HealthProfileUiEffect = FoundationUiEffect

class HealthProfileViewModel(repository: LocalHealthEntryRepository) :
    FoundationFeatureViewModel(FoundationFeature.HealthProfile, repository)

@Composable
fun HealthProfileRoute(viewModel: HealthProfileViewModel = foundationFeatureViewModel("health-profile", HealthProfileViewModel::class) { HealthProfileViewModel(it) }) {
    FoundationFeatureRoute(viewModel) { state, onEvent -> HealthProfileScreen(state, onEvent) }
}

@Composable
fun HealthProfileScreen(state: HealthProfileUiState, onEvent: (HealthProfileUiEvent) -> Unit) {
    FoundationFeatureScreen(FoundationFeature.HealthProfile, state, onEvent)
}

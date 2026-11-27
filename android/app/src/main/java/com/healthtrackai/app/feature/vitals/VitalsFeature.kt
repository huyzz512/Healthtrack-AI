package com.healthtrackai.app.feature.vitals

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

typealias VitalsUiState = FoundationUiState
typealias VitalsUiEvent = FoundationUiEvent
typealias VitalsUiEffect = FoundationUiEffect

class VitalsViewModel(repository: LocalHealthEntryRepository) :
    FoundationFeatureViewModel(FoundationFeature.Vitals, repository)

@Composable
fun VitalsRoute(viewModel: VitalsViewModel = foundationFeatureViewModel("vitals", VitalsViewModel::class) { VitalsViewModel(it) }) {
    FoundationFeatureRoute(viewModel) { state, onEvent -> VitalsScreen(state, onEvent) }
}

@Composable
fun VitalsScreen(state: VitalsUiState, onEvent: (VitalsUiEvent) -> Unit) {
    FoundationFeatureScreen(FoundationFeature.Vitals, state, onEvent)
}

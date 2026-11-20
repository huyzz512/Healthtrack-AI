package com.healthtrackai.app.feature.reports

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthtrackai.app.core.presentation.FoundationFeature
import com.healthtrackai.app.core.presentation.FoundationFeatureRoute
import com.healthtrackai.app.core.presentation.FoundationFeatureScreen
import com.healthtrackai.app.core.presentation.FoundationFeatureViewModel
import com.healthtrackai.app.core.presentation.FoundationUiEffect
import com.healthtrackai.app.core.presentation.FoundationUiEvent
import com.healthtrackai.app.core.presentation.FoundationUiState

typealias ReportsUiState = FoundationUiState
typealias ReportsUiEvent = FoundationUiEvent
typealias ReportsUiEffect = FoundationUiEffect

class ReportsViewModel : FoundationFeatureViewModel(FoundationFeature.Reports)

@Composable
fun ReportsRoute(viewModel: ReportsViewModel = viewModel()) {
    FoundationFeatureRoute(viewModel) { state, onEvent -> ReportsScreen(state, onEvent) }
}

@Composable
fun ReportsScreen(state: ReportsUiState, onEvent: (ReportsUiEvent) -> Unit) {
    FoundationFeatureScreen(FoundationFeature.Reports, state, onEvent)
}

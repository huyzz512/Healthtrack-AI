package com.healthtrackai.app.feature.bodymetrics

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthtrackai.app.core.presentation.FoundationFeature
import com.healthtrackai.app.core.presentation.FoundationFeatureRoute
import com.healthtrackai.app.core.presentation.FoundationFeatureScreen
import com.healthtrackai.app.core.presentation.FoundationFeatureViewModel
import com.healthtrackai.app.core.presentation.FoundationUiEffect
import com.healthtrackai.app.core.presentation.FoundationUiEvent
import com.healthtrackai.app.core.presentation.FoundationUiState

typealias BodyMetricsUiState = FoundationUiState
typealias BodyMetricsUiEvent = FoundationUiEvent
typealias BodyMetricsUiEffect = FoundationUiEffect

class BodyMetricsViewModel : FoundationFeatureViewModel(FoundationFeature.BodyMetrics)

@Composable
fun BodyMetricsRoute(viewModel: BodyMetricsViewModel = viewModel()) {
    FoundationFeatureRoute(viewModel) { state, onEvent -> BodyMetricsScreen(state, onEvent) }
}

@Composable
fun BodyMetricsScreen(state: BodyMetricsUiState, onEvent: (BodyMetricsUiEvent) -> Unit) {
    FoundationFeatureScreen(FoundationFeature.BodyMetrics, state, onEvent)
}

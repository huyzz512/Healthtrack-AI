package com.healthtrackai.app.feature.analytics

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthtrackai.app.core.presentation.FoundationFeature
import com.healthtrackai.app.core.presentation.FoundationFeatureRoute
import com.healthtrackai.app.core.presentation.FoundationFeatureScreen
import com.healthtrackai.app.core.presentation.FoundationFeatureViewModel
import com.healthtrackai.app.core.presentation.FoundationUiEffect
import com.healthtrackai.app.core.presentation.FoundationUiEvent
import com.healthtrackai.app.core.presentation.FoundationUiState

typealias AnalyticsUiState = FoundationUiState
typealias AnalyticsUiEvent = FoundationUiEvent
typealias AnalyticsUiEffect = FoundationUiEffect

class AnalyticsViewModel : FoundationFeatureViewModel(FoundationFeature.Analytics)

@Composable
fun AnalyticsRoute(viewModel: AnalyticsViewModel = viewModel()) {
    FoundationFeatureRoute(viewModel) { state, onEvent -> AnalyticsScreen(state, onEvent) }
}

@Composable
fun AnalyticsScreen(state: AnalyticsUiState, onEvent: (AnalyticsUiEvent) -> Unit) {
    FoundationFeatureScreen(FoundationFeature.Analytics, state, onEvent)
}

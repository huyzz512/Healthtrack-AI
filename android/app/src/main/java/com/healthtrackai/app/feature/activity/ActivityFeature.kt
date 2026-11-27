package com.healthtrackai.app.feature.activity

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

typealias ActivityUiState = FoundationUiState
typealias ActivityUiEvent = FoundationUiEvent
typealias ActivityUiEffect = FoundationUiEffect

class ActivityViewModel(repository: LocalHealthEntryRepository) :
    FoundationFeatureViewModel(FoundationFeature.Activity, repository)

@Composable
fun ActivityRoute(viewModel: ActivityViewModel = foundationFeatureViewModel("activity", ActivityViewModel::class) { ActivityViewModel(it) }) {
    FoundationFeatureRoute(viewModel) { state, onEvent -> ActivityScreen(state, onEvent) }
}

@Composable
fun ActivityScreen(state: ActivityUiState, onEvent: (ActivityUiEvent) -> Unit) {
    FoundationFeatureScreen(FoundationFeature.Activity, state, onEvent)
}

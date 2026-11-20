package com.healthtrackai.app.feature.auth

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthtrackai.app.core.presentation.FoundationFeature
import com.healthtrackai.app.core.presentation.FoundationFeatureRoute
import com.healthtrackai.app.core.presentation.FoundationFeatureScreen
import com.healthtrackai.app.core.presentation.FoundationFeatureViewModel
import com.healthtrackai.app.core.presentation.FoundationUiEffect
import com.healthtrackai.app.core.presentation.FoundationUiEvent
import com.healthtrackai.app.core.presentation.FoundationUiState

typealias AuthUiState = FoundationUiState
typealias AuthUiEvent = FoundationUiEvent
typealias AuthUiEffect = FoundationUiEffect

class AuthViewModel : FoundationFeatureViewModel(FoundationFeature.Auth)

@Composable
fun AuthRoute(viewModel: AuthViewModel = viewModel()) {
    FoundationFeatureRoute(viewModel) { state, onEvent -> AuthScreen(state, onEvent) }
}

@Composable
fun AuthScreen(state: AuthUiState, onEvent: (AuthUiEvent) -> Unit) {
    FoundationFeatureScreen(FoundationFeature.Auth, state, onEvent)
}

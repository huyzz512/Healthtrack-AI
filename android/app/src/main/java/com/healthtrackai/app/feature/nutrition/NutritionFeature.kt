package com.healthtrackai.app.feature.nutrition

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthtrackai.app.core.presentation.FoundationFeature
import com.healthtrackai.app.core.presentation.FoundationFeatureRoute
import com.healthtrackai.app.core.presentation.FoundationFeatureScreen
import com.healthtrackai.app.core.presentation.FoundationFeatureViewModel
import com.healthtrackai.app.core.presentation.FoundationUiEffect
import com.healthtrackai.app.core.presentation.FoundationUiEvent
import com.healthtrackai.app.core.presentation.FoundationUiState

typealias NutritionUiState = FoundationUiState
typealias NutritionUiEvent = FoundationUiEvent
typealias NutritionUiEffect = FoundationUiEffect

class NutritionViewModel : FoundationFeatureViewModel(FoundationFeature.Nutrition)

@Composable
fun NutritionRoute(viewModel: NutritionViewModel = viewModel()) {
    FoundationFeatureRoute(viewModel) { state, onEvent -> NutritionScreen(state, onEvent) }
}

@Composable
fun NutritionScreen(state: NutritionUiState, onEvent: (NutritionUiEvent) -> Unit) {
    FoundationFeatureScreen(FoundationFeature.Nutrition, state, onEvent)
}

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
import com.healthtrackai.app.core.presentation.foundationFeatureViewModel
import com.healthtrackai.app.core.domain.LocalHealthEntryRepository

typealias NutritionUiState = FoundationUiState
typealias NutritionUiEvent = FoundationUiEvent
typealias NutritionUiEffect = FoundationUiEffect

class NutritionViewModel(repository: LocalHealthEntryRepository) :
    FoundationFeatureViewModel(FoundationFeature.Nutrition, repository)

@Composable
fun NutritionRoute(viewModel: NutritionViewModel = foundationFeatureViewModel("nutrition", NutritionViewModel::class) { NutritionViewModel(it) }) {
    FoundationFeatureRoute(viewModel) { state, onEvent -> NutritionScreen(state, onEvent) }
}

@Composable
fun NutritionScreen(state: NutritionUiState, onEvent: (NutritionUiEvent) -> Unit) {
    FoundationFeatureScreen(FoundationFeature.Nutrition, state, onEvent)
}

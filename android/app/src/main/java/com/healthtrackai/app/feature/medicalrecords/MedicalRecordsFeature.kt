package com.healthtrackai.app.feature.medicalrecords

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthtrackai.app.core.presentation.FoundationFeature
import com.healthtrackai.app.core.presentation.FoundationFeatureRoute
import com.healthtrackai.app.core.presentation.FoundationFeatureScreen
import com.healthtrackai.app.core.presentation.FoundationFeatureViewModel
import com.healthtrackai.app.core.presentation.FoundationUiEffect
import com.healthtrackai.app.core.presentation.FoundationUiEvent
import com.healthtrackai.app.core.presentation.FoundationUiState

typealias MedicalRecordsUiState = FoundationUiState
typealias MedicalRecordsUiEvent = FoundationUiEvent
typealias MedicalRecordsUiEffect = FoundationUiEffect

class MedicalRecordsViewModel : FoundationFeatureViewModel(FoundationFeature.MedicalRecords)

@Composable
fun MedicalRecordsRoute(viewModel: MedicalRecordsViewModel = viewModel()) {
    FoundationFeatureRoute(viewModel) { state, onEvent -> MedicalRecordsScreen(state, onEvent) }
}

@Composable
fun MedicalRecordsScreen(state: MedicalRecordsUiState, onEvent: (MedicalRecordsUiEvent) -> Unit) {
    FoundationFeatureScreen(FoundationFeature.MedicalRecords, state, onEvent)
}

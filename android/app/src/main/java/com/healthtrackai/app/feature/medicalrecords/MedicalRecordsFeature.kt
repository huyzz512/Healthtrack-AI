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
import com.healthtrackai.app.core.presentation.foundationFeatureViewModel
import com.healthtrackai.app.core.domain.LocalHealthEntryRepository

typealias MedicalRecordsUiState = FoundationUiState
typealias MedicalRecordsUiEvent = FoundationUiEvent
typealias MedicalRecordsUiEffect = FoundationUiEffect

class MedicalRecordsViewModel(repository: LocalHealthEntryRepository) :
    FoundationFeatureViewModel(FoundationFeature.MedicalRecords, repository)

@Composable
fun MedicalRecordsRoute(viewModel: MedicalRecordsViewModel = foundationFeatureViewModel("medical-records", MedicalRecordsViewModel::class) { MedicalRecordsViewModel(it) }) {
    FoundationFeatureRoute(viewModel) { state, onEvent -> MedicalRecordsScreen(state, onEvent) }
}

@Composable
fun MedicalRecordsScreen(state: MedicalRecordsUiState, onEvent: (MedicalRecordsUiEvent) -> Unit) {
    FoundationFeatureScreen(FoundationFeature.MedicalRecords, state, onEvent)
}

package com.healthtrackai.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.healthtrackai.app.feature.dashboard.DashboardScreen
import com.healthtrackai.app.feature.dashboard.DashboardUiEvent
import com.healthtrackai.app.feature.dashboard.DashboardUiState

@Composable
fun HealthTrackNavHost() {
    var state by remember { mutableStateOf(DashboardUiState()) }

    DashboardScreen(
        state = state,
        onEvent = { event ->
            if (event == DashboardUiEvent.AddWater250Ml) {
                state = state.copy(waterIntakeMl = (state.waterIntakeMl + 250).coerceAtMost(2_000))
            }
        },
    )
}

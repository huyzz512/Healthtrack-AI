package com.healthtrackai.app.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.healthtrackai.app.core.presentation.FoundationFeature
import com.healthtrackai.app.feature.activity.ActivityRoute
import com.healthtrackai.app.feature.aiassistant.AiAssistantRoute
import com.healthtrackai.app.feature.analytics.AnalyticsRoute
import com.healthtrackai.app.feature.auth.AuthRoute
import com.healthtrackai.app.feature.bodymetrics.BodyMetricsRoute
import com.healthtrackai.app.feature.dashboard.DashboardRoute
import com.healthtrackai.app.feature.healthconnect.HealthConnectRoute
import com.healthtrackai.app.feature.healthconnect.HealthConnectViewModel
import com.healthtrackai.app.feature.healthprofile.HealthProfileRoute
import com.healthtrackai.app.feature.medicalrecords.MedicalRecordsRoute
import com.healthtrackai.app.feature.mood.MoodRoute
import com.healthtrackai.app.feature.nutrition.NutritionRoute
import com.healthtrackai.app.feature.reminders.RemindersRoute
import com.healthtrackai.app.feature.reports.ReportsRoute
import com.healthtrackai.app.feature.vitals.VitalsRoute
import kotlinx.serialization.Serializable

@Serializable
private data object DashboardDestination

@Serializable
private data class FeatureDestination(val featureRoute: String)

@Composable
fun HealthTrackNavHost(
    navController: NavHostController = rememberNavController(),
    healthConnectViewModel: HealthConnectViewModel = viewModel(),
) {
    NavHost(
        navController = navController,
        startDestination = DashboardDestination,
    ) {
        composable<DashboardDestination> {
            DashboardRoute(
                onNavigate = { feature ->
                    navController.navigate(FeatureDestination(feature.route))
                },
            )
        }
        composable<FeatureDestination> { backStackEntry ->
            val destination = backStackEntry.toRoute<FeatureDestination>()
            when (FoundationFeature.fromRoute(destination.featureRoute)) {
                FoundationFeature.Auth -> AuthRoute()
                FoundationFeature.Dashboard -> DashboardRoute(
                    onNavigate = { selected ->
                        navController.navigate(FeatureDestination(selected.route))
                    },
                )

                FoundationFeature.MedicalRecords -> MedicalRecordsRoute()
                FoundationFeature.HealthProfile -> HealthProfileRoute()
                FoundationFeature.BodyMetrics -> BodyMetricsRoute()
                FoundationFeature.Vitals -> VitalsRoute()
                FoundationFeature.Reminders -> RemindersRoute()
                FoundationFeature.Nutrition -> NutritionRoute()
                FoundationFeature.Mood -> MoodRoute()
                FoundationFeature.HealthConnect -> HealthConnectRoute(healthConnectViewModel)
                FoundationFeature.AiAssistant -> AiAssistantRoute()
                FoundationFeature.Activity -> ActivityRoute()
                FoundationFeature.Analytics -> AnalyticsRoute()
                FoundationFeature.Reports -> ReportsRoute()
                null -> UnknownDestinationScreen(onBack = navController::popBackStack)
            }
        }
    }
}

@Composable
private fun UnknownDestinationScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("This destination is not supported by the current app version.")
        Button(onClick = onBack) { Text("Back") }
    }
}

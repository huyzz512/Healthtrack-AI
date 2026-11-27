package com.healthtrackai.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.healthtrackai.app.core.presentation.FoundationFeature
import com.healthtrackai.app.feature.account.AccountRoute
import com.healthtrackai.app.feature.auth.AuthRoute
import com.healthtrackai.app.feature.dashboard.DashboardRoute

private const val DASHBOARD_ROUTE = "dashboard"
private const val AUTH_ROUTE = "auth"
private const val ACCOUNT_ROUTE = "account"

@Composable
fun HealthTrackNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = DASHBOARD_ROUTE,
    ) {
        composable(DASHBOARD_ROUTE) {
            DashboardRoute(
                onNavigate = { feature ->
                    when (feature) {
                        FoundationFeature.Auth -> navController.navigate(AUTH_ROUTE)
                        FoundationFeature.Account -> navController.navigate(ACCOUNT_ROUTE)
                        else -> Unit
                    }
                },
            )
        }
        composable(AUTH_ROUTE) {
            AuthRoute(
                onAuthenticated = {
                    navController.navigate(ACCOUNT_ROUTE) {
                        popUpTo(DASHBOARD_ROUTE)
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(ACCOUNT_ROUTE) {
            AccountRoute(
                onSignIn = { navController.navigate(AUTH_ROUTE) },
                onSignedOut = {
                    navController.popBackStack(DASHBOARD_ROUTE, inclusive = false)
                },
            )
        }
    }
}

package com.healthtrackai.app.core.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
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
    isDarkMode: Boolean = false,
    onDarkModeChange: (Boolean) -> Unit = {},
) {
    NavHost(
        navController = navController,
        startDestination = DASHBOARD_ROUTE,
        enterTransition = { fadeIn(animationSpec = tween(220)) },
        exitTransition = { fadeOut(animationSpec = tween(220)) },
        popEnterTransition = { fadeIn(animationSpec = tween(220)) },
        popExitTransition = { fadeOut(animationSpec = tween(220)) },
    ) {
        composable(DASHBOARD_ROUTE) {
            DashboardRoute(
                isDarkMode = isDarkMode,
                onDarkModeChange = onDarkModeChange,
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

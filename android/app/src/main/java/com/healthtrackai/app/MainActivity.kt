package com.healthtrackai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.healthtrackai.app.core.designsystem.HealthTrackTheme
import com.healthtrackai.app.core.navigation.HealthTrackNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkModeEnabled by rememberSaveable { mutableStateOf(false) }
            SideEffect {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = !darkModeEnabled
                insetsController.isAppearanceLightNavigationBars = !darkModeEnabled
            }
            HealthTrackTheme(darkTheme = darkModeEnabled) {
                HealthTrackNavHost(
                    isDarkMode = darkModeEnabled,
                    onDarkModeChange = { darkModeEnabled = it },
                )
            }
        }
    }
}

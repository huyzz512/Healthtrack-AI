package com.healthtrackai.app.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.ui.tooling.preview.Preview
import com.healthtrackai.app.core.designsystem.HealthTrackTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthtrackai.app.core.di.healthTrackEntryPoint
import com.healthtrackai.app.core.network.contract.FoundationHealthApi
import com.healthtrackai.app.core.presentation.FoundationFeature
import com.healthtrackai.app.ui.screens.insights.InsightsScreen
import com.healthtrackai.app.ui.screens.detail.HealthDetailScreen
import com.healthtrackai.app.ui.screens.overview.OverviewScreen
import com.healthtrackai.app.ui.screens.profile.ProfileScreen
import com.healthtrackai.app.ui.screens.today.TodayScreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

enum class DashboardTab(val title: String) {
    HOME("Tổng quan"),
    TODAY("Hôm nay"),
    SOURCES("Nguồn"),
    PROFILE("Hồ sơ"),
}

private fun dashboardTabIcon(tab: DashboardTab, selected: Boolean): ImageVector = when (tab) {
    DashboardTab.HOME -> Icons.Rounded.Home
    DashboardTab.TODAY -> Icons.Rounded.CalendarToday
    DashboardTab.SOURCES -> Icons.Rounded.BarChart
    DashboardTab.PROFILE -> Icons.Rounded.Person
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val destinations: List<FoundationFeature> = FoundationFeature.dashboardDestinations,
    val errorMessage: String? = null,
    val backendStatus: BackendStatus = BackendStatus.CHECKING,
    val selectedTab: DashboardTab = DashboardTab.HOME,
    val selectedDayOffset: Int = 0,
    val waterIntakeMl: Int = 1_500,
    val isQuickAddOpen: Boolean = false,
)

enum class BackendStatus { CHECKING, CONNECTED, UNAVAILABLE }

sealed interface DashboardUiEvent {
    data class SelectFeature(val feature: FoundationFeature) : DashboardUiEvent
    data class SelectTab(val tab: DashboardTab) : DashboardUiEvent
    data class SelectDay(val offset: Int) : DashboardUiEvent
    data object AddWater250Ml : DashboardUiEvent
    data object OpenQuickAdd : DashboardUiEvent
    data object CloseQuickAdd : DashboardUiEvent
    data object Retry : DashboardUiEvent
}

sealed interface DashboardUiEffect {
    data class NavigateToFeature(val feature: FoundationFeature) : DashboardUiEffect
    data class ShowMessage(val message: String) : DashboardUiEffect
}

class DashboardViewModel(private val healthApi: FoundationHealthApi) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private val effectChannel = Channel<DashboardUiEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    init { refreshBackend() }

    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            is DashboardUiEvent.SelectFeature -> effectChannel.trySend(DashboardUiEffect.NavigateToFeature(event.feature))
            is DashboardUiEvent.SelectTab -> _uiState.value = _uiState.value.copy(selectedTab = event.tab)
            is DashboardUiEvent.SelectDay -> _uiState.value = _uiState.value.copy(selectedDayOffset = event.offset)
            DashboardUiEvent.AddWater250Ml -> _uiState.value = _uiState.value.copy(waterIntakeMl = (_uiState.value.waterIntakeMl + 250).coerceAtMost(2_000))
            DashboardUiEvent.OpenQuickAdd -> _uiState.value = _uiState.value.copy(isQuickAddOpen = true)
            DashboardUiEvent.CloseQuickAdd -> _uiState.value = _uiState.value.copy(isQuickAddOpen = false)
            DashboardUiEvent.Retry -> {
                _uiState.value = _uiState.value.copy(backendStatus = BackendStatus.CHECKING)
                effectChannel.trySend(DashboardUiEffect.ShowMessage("Đang kiểm tra lại kết nối máy chủ."))
                refreshBackend()
            }
        }
    }

    private fun refreshBackend() {
        viewModelScope.launch {
            val status = runCatching {
                val health = healthApi.health().data.toDomain()
                if (health.status == com.healthtrackai.app.core.network.contract.ServiceStatus.OK &&
                    health.database == com.healthtrackai.app.core.network.contract.DatabaseStatus.CONNECTED
                ) BackendStatus.CONNECTED else BackendStatus.UNAVAILABLE
            }.getOrDefault(BackendStatus.UNAVAILABLE)
            _uiState.value = _uiState.value.copy(backendStatus = status)
        }
    }
}

@Composable
private fun dashboardViewModel(): DashboardViewModel {
    val context = LocalContext.current.applicationContext
    val api = remember(context) { healthTrackEntryPoint(context).foundationHealthApi() }
    val factory = remember(api) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = DashboardViewModel(api) as T
        }
    }
    return viewModel(factory = factory)
}

@Composable
fun DashboardRoute(
    onNavigate: (FoundationFeature) -> Unit,
    viewModel: DashboardViewModel = dashboardViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel, onNavigate) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DashboardUiEffect.NavigateToFeature -> onNavigate(effect.feature)
                is DashboardUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        DashboardScreen(
            state = state,
            onEvent = viewModel::onEvent,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabsNavController = rememberNavController()
    val backStackEntry by tabsNavController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: "overview"
    fun navigateToTab(tab: DashboardTab) {
        tabsNavController.navigate(tab.route, navOptions {
            popUpTo(tabsNavController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        })
    }
    val navigateToFeature: (FoundationFeature) -> Unit = { feature ->
        if (feature in setOf(
                FoundationFeature.Activity,
                FoundationFeature.Mood,
                FoundationFeature.Nutrition,
                FoundationFeature.Reminders,
                FoundationFeature.Sleep,
                FoundationFeature.BodyMetrics,
                FoundationFeature.Vitals,
                FoundationFeature.MedicalRecords,
                FoundationFeature.HealthProfile,
                FoundationFeature.HealthConnect,
            )
        ) {
            tabsNavController.navigate("detail/${feature.route}") {
                launchSingleTop = true
            }
        } else {
        val tab = when (feature) {
            FoundationFeature.Activity,
            FoundationFeature.Mood,
            FoundationFeature.Nutrition,
            FoundationFeature.Reminders -> DashboardTab.TODAY
            FoundationFeature.Analytics -> DashboardTab.SOURCES
            FoundationFeature.HealthProfile,
            FoundationFeature.BodyMetrics,
            FoundationFeature.Vitals,
            FoundationFeature.MedicalRecords -> DashboardTab.PROFILE
            else -> DashboardTab.HOME
        }
        navigateToTab(tab)
        }
    }
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(60.dp).drawBehind {
                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 0.5.dp.toPx(),
                    )
                },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                DashboardTab.entries.forEach { tab ->
                    val selected = currentRoute == tab.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = { navigateToTab(tab) },
                        icon = {
                            Box(
                                modifier = Modifier.size(width = 52.dp, height = 28.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (selected) Color(0xFFCCFBF1) else Color.Transparent),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(dashboardTabIcon(tab, selected), contentDescription = null, modifier = Modifier.size(22.dp))
                            }
                        },
                        label = { Text(tab.title, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedIconColor = Color(0xFF0F766E),
                            selectedTextColor = Color(0xFF0F766E),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B),
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = tabsNavController,
            startDestination = DashboardTab.HOME.route,
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
        ) {
            composable(DashboardTab.HOME.route) { OverviewScreen(state, onEvent, navigateToFeature) }
            composable(DashboardTab.TODAY.route) { TodayScreen() }
            composable(DashboardTab.SOURCES.route) { InsightsScreen() }
            composable(DashboardTab.PROFILE.route) { ProfileScreen(onNavigate = navigateToFeature) }
            composable(
                route = "detail/{featureRoute}",
                arguments = listOf(navArgument("featureRoute") { type = NavType.StringType }),
            ) { entry ->
                val feature = FoundationFeature.fromRoute(entry.arguments?.getString("featureRoute").orEmpty())
                HealthDetailScreen(feature = feature, onBack = { tabsNavController.popBackStack() })
            }
        }
    }
}

private val DashboardTab.route: String
    get() = when (this) {
        DashboardTab.HOME -> "overview"
        DashboardTab.TODAY -> "today"
        DashboardTab.SOURCES -> "insights"
        DashboardTab.PROFILE -> "profile"
    }

@Preview(showBackground = true, name = "Trang chủ · Tổng quan", widthDp = 393, heightDp = 852)
@Composable
private fun DashboardOverviewPreview() {
    HealthTrackTheme {
        DashboardScreen(
            state = DashboardUiState(backendStatus = BackendStatus.UNAVAILABLE),
            onEvent = {},
        )
    }
}

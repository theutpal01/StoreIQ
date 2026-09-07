package com.storiq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomNavigation
import androidx.compose.material3.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.ViewModel
import androidx.lifecycle.viewmodel.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.storiq.core.ui.theme.StorIQTheme
import com.storiq.feature.analyzer.ui.AnalyzerScreen
import com.storiq.feature.apps.ui.AppsScreen
import com.storiq.feature.cleanup.ui.CleanScreen
import com.storiq.feature.dashboard.ui.DashboardScreen
import com.storiq.feature.duplicates.ui.DuplicatesScreen
import com.storiq.feature.history.ui.HistoryScreen
import com.storiq.feature.largefiles.ui.LargeFilesScreen
import com.storiq.feature.more.ui.MoreScreen
import com.storiq.feature.onboarding.ui.OnboardingScreen
import com.storiq.feature.swipeclean.ui.SwipeCleanGalleryScreen
import com.storiq.feature.swipeclean.ui.SwipeCleanReviewScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.preferencesKey
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.MoreHoriz

class MainActivity : ComponentActivity() {
    private val onboardingViewModel: OnboardingViewModel by viewModels()
    private val preferencesDataStore = androidx.datastore.preferences.preferencesDataStore(this, "storiq_preferences")
    private val storageRepository by lazy {
        com.storiq.AppEntryPoint.get(application).storageRepository()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StorIQTheme {
                val navController = rememberNavController()
                val onboardingCompleted by onboardingViewModel.onboardingCompleted.collectAsStateWithLifecycle()

                if (!onboardingCompleted) {
                    OnboardingScreen {
                        onboardingViewModel.completeOnboarding()
                    }
                } else {
                    StorIQNavHost(navController, storageRepository)
                }
            }
        }
    }
}

class OnboardingViewModel : ViewModel() {
    private val _onboardingCompleted = mutableStateOf(false)
    val onboardingCompleted = _onboardingCompleted

    init {
        checkOnboardingStatus()
    }

    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            // Check DataStore for onboarding completion
            val result = preferencesDataStore.data
                .map { it[booleanPreferencesKey("onboarding_completed")] ?: false }
                .first()
            _onboardingCompleted.value = result
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesDataStore.edit { it[booleanPreferencesKey("onboarding_completed")] = true }
            _onboardingCompleted.value = true
        }
    }
}

@Composable
fun StorIQNavHost(navController: NavController, storageRepository: StorageRepository) {
    val dashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = DashboardViewModel.Factory(storageRepository)
    )
    val historyViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = HistoryViewModelFactory(storageRepository)
    )
    val largeFilesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = LargeFilesViewModelFactory(storageRepository)
    )
    val duplicatesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = DuplicatesViewModelFactory(storageRepository)
    )
    NavHost(navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToAnalyze = { navController.navigate("analyze") },
                onNavigateToClean = { navController.navigate("clean") },
                onNavigateToApps = { navController.navigate("apps") },
                // onNavigateToSwipeClean = { navController.navigate("swipe_clean") }, // TODO: Fix SwipeClean feature
                onNavigateToLargeFiles = { navController.navigate("large_files") },
                onNavigateToDuplicates = { navController.navigate("duplicates") }
            )
        }
        composable("analyze") {
            AnalyzerScreen()
        }
        composable("clean") {
            CleanScreen()
        }
        composable("apps") {
            AppsScreen()
        }
        composable("more") {
            MoreScreen(
                onNavigateToHistory = { navController.navigate("history") },
                // onNavigateToSwipeClean = { navController.navigate("swipe_clean") }, // TODO: Fix SwipeClean feature
                onNavigateToFileExplorer = { navController.navigate("file_explorer") },
                onNavigateToPrivacy = { navController.navigate("privacy") },
                onNavigateToDeviceInfo = { navController.navigate("device_info") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("swipe_clean") {
            SwipeCleanGalleryScreen(
                viewModel = swipeCleanViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("swipe_clean_review") {
            SwipeCleanReviewScreen(
                viewModel = swipeCleanViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("history") {
            HistoryScreen(
                viewModel = historyViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("large_files") {
            LargeFilesScreen(
                viewModel = largeFilesViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("duplicates") {
            DuplicatesScreen(
                viewModel = duplicatesViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("file_explorer") {
            androidx.compose.material3.Text("File Explorer - Coming in Phase 3", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, modifier = androidx.compose.foundation.layout.Modifier.fillMaxSize().padding(16.dp))
        }
        composable("privacy") {
            androidx.compose.material3.Text("Privacy Dashboard - Coming in Phase 3", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, modifier = androidx.compose.foundation.layout.Modifier.fillMaxSize().padding(16.dp))
        }
        composable("device_info") {
            androidx.compose.material3.Text("Device Info - Coming in Phase 3", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, modifier = androidx.compose.foundation.layout.Modifier.fillMaxSize().padding(16.dp))
        }
        composable("settings") {
            androidx.compose.material3.Text("Settings - Coming in Phase 3", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, modifier = androidx.compose.foundation.layout.Modifier.fillMaxSize().padding(16.dp))
        }
    }
}

@Composable
fun StorIQBottomNavigation(navController: NavController) {
    val items = listOf(
        NavigationItem("dashboard", "Home", Icons.Filled.Home),
        NavigationItem("analyze", "Analyze", Icons.Filled.Analytics),
        NavigationItem("clean", "Clean", Icons.Filled.CleaningServices),
        NavigationItem("apps", "Apps", Icons.Filled.Apps),
        NavigationItem("more", "More", Icons.Filled.MoreHoriz)
    )

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
    ) {
        items.forEach { item ->
            val selected = navController.currentBackStackEntryAsState().value?.destination?.route?.startsWith(item.route) == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = if (selected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}

data class NavigationItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
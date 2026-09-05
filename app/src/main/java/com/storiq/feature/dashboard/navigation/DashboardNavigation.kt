package com.storiq.feature.dashboard.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import com.storiq.feature.dashboard.ui.DashboardScreen

fun NavGraphBuilder.dashboardGraph() {
    composable(route = "dashboard") {
        DashboardScreen()
    }
}
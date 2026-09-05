package com.storiq.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import com.storiq.feature.onboarding.ui.OnboardingScreen

fun NavGraphBuilder.onboardingGraph(
    onFinish: () -> Unit
) {
    composable(route = "onboarding") {
        OnboardingScreen(onFinish = onFinish)
    }
}
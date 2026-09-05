package com.storiq.feature.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.storiq.core.ui.components.PermissionState
import com.storiq.core.ui.theme.StorIQGreen
import com.storiq.core.ui.theme.Typography

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    var showScanScreen by remember { mutableStateOf(false) }
    val pages = remember {
        listOf(
            OnboardingPage(
                title = "StorIQ",
                subtitle = "Understand your device.\nTake back your space.",
                illustration = "📱",
                buttonText = "Get Started"
            ),
            OnboardingPage(
                title = "Your Privacy Matters",
                subtitle = "Storage analysis happens on your device\nwhenever possible. No data leaves your phone\nwithout your explicit consent.",
                illustration = "🔒",
                buttonText = "Continue"
            ),
            OnboardingPage(
                title = "Grant Access to Begin",
                subtitle = "We need permission to access your media\nto analyze storage and find duplicates.",
                illustration = "📷",
                buttonText = "Allow Access",
                isPermissionScreen = true
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showScanScreen) {
            com.storiq.feature.onboarding.ui.InitialScanScreen(
                repository = com.storiq.AppEntryPoint.get(
                    androidx.compose.ui.platform.LocalContext.current.applicationContext
                ).storageRepository(),
                onScanComplete = { result ->
                    if (result.success) {
                        onFinish()
                    } else {
                        // Show error, allow retry
                        showScanScreen = false
                        currentPage = 2 // Back to permission screen
                    }
                }
            )
        } else {
            OnboardingPageView(
                page = pages[currentPage],
                currentStep = currentPage + 1,
                totalSteps = pages.size,
                onNext = {
                    if (currentPage < pages.size - 1) {
                        currentPage++
                    } else {
                        showScanScreen = true
                    }
                },
                onSkip = { onFinish() }
            )
        }
    }
}

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val illustration: String,
    val buttonText: String,
    val isPermissionScreen: Boolean = false
)

@Composable
fun OnboardingPageView(
    page: OnboardingPage,
    currentStep: Int,
    totalSteps: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            (1..totalSteps).forEach { step ->
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(if (step == currentStep) 12.dp else 8.dp)
                        .padding(horizontal = 4.dp)
                        .background(
                            color = if (step <= currentStep) StorIQGreen else Color.LightGray,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        )
                )
            }
        }

        // Illustration
        Text(
            text = page.illustration,
            fontSize = 80.sp
        )

        // Title
        Text(
            text = page.title,
            style = Typography.headlineLarge,
            textAlign = androidx.compose.ui.text.TextAlign.Center
        )

        // Subtitle
        Text(
            text = page.subtitle,
            style = Typography.bodyLarge,
            textAlign = androidx.compose.ui.text.TextAlign.Center,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Buttons
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (page.isPermissionScreen) {
                PermissionState(
                    title = "Photos and Videos",
                    message = "StorIQ needs access to analyze your media and find duplicates or large files.",
                    onGrant = onNext,
                    onContinue = onSkip
                )
            } else {
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StorIQGreen
                    )
                ) {
                    Text(
                        text = page.buttonText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                if (currentStep < totalSteps) {
                    Text(
                        text = "Skip",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    ).let { text ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(48.dp)
                                .background(Color.Transparent)
                                .clickable { onSkip() }
                                .padding(vertical = 8.dp)
                        ) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                text
                            }
                        }
                    }
                }
            }
        }
    }
}
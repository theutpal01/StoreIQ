package com.storiq.feature.onboarding

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class OnboardingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun onboardingScreen_displaysFirstPage() {
        composeRule.setContent {
            com.storiq.feature.onboarding.ui.OnboardingScreen(onFinish = {})
        }

        composeRule.onNodeWithText("StorIQ").assertIsDisplayed()
        composeRule.onNodeWithText("Understand your device.\nTake back your space.").assertIsDisplayed()
        composeRule.onNodeWithText("Get Started").assertIsDisplayed()
    }

    @Test
    fun onboardingScreen_navigatesToSecondPage() {
        composeRule.setContent {
            com.storiq.feature.onboarding.ui.OnboardingScreen(onFinish = {})
        }

        composeRule.onNodeWithText("Get Started").performClick()

        composeRule.onNodeWithText("Your Privacy Matters").assertIsDisplayed()
        composeRule.onNodeWithText("Storage analysis happens on your device\nwhenever possible.").assertIsDisplayed()
        composeRule.onNodeWithText("Continue").assertIsDisplayed()
    }

    @Test
    fun onboardingScreen_navigatesToThirdPage() {
        composeRule.setContent {
            com.storiq.feature.onboarding.ui.OnboardingScreen(onFinish = {})
        }

        composeRule.onNodeWithText("Get Started").performClick()
        composeRule.onNodeWithText("Continue").performClick()

        composeRule.onNodeWithText("Grant Access to Begin").assertIsDisplayed()
        composeRule.onNodeWithText("Allow Access").assertIsDisplayed()
    }
}
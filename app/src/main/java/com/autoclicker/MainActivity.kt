package com.autoclicker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.autoclicker.service.AutoClickerAccessibilityService
import com.autoclicker.service.OverlayService
import com.autoclicker.ui.screens.OnboardingScreen
import com.autoclicker.ui.screens.ProfileEditScreen
import com.autoclicker.ui.screens.ProfileListScreen
import com.autoclicker.ui.theme.AutoClickerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoClickerTheme {
                AutoClickerApp()
            }
        }
    }
}

@Composable
fun AutoClickerApp() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val hasAccessibility by remember {
        derivedStateOf { AutoClickerAccessibilityService.instance != null }
    }
    val hasOverlay by remember {
        derivedStateOf { Settings.canDrawOverlays(context) }
    }
    val isOverlayRunning by OverlayService.isRunning

    val startDest = if (!hasAccessibility || !hasOverlay) "onboarding" else "list"

    NavHost(navController = navController, startDestination = startDest) {

        composable("onboarding") {
            OnboardingScreen(
                hasAccessibility = hasAccessibility,
                hasOverlay = hasOverlay,
                onComplete = {
                    navController.navigate("list") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("list") {
            ProfileListScreen(
                onEditProfile = { id -> navController.navigate("edit/$id") },
                onStartOverlay = {
                    if (isOverlayRunning) {
                        context.startService(
                            Intent(context, OverlayService::class.java)
                                .apply { action = OverlayService.ACTION_STOP }
                        )
                    } else {
                        context.startForegroundService(
                            Intent(context, OverlayService::class.java)
                                .apply { action = OverlayService.ACTION_START }
                        )
                    }
                },
                isOverlayRunning = isOverlayRunning
            )
        }

        composable(
            route = "edit/{profileId}",
            arguments = listOf(navArgument("profileId") { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments!!.getLong("profileId")
            ProfileEditScreen(
                profileId = id,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

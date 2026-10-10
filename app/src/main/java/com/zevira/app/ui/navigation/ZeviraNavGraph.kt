package com.zevira.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.zevira.app.data.local.preferences.UserPreferencesDataStore
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zevira.app.ui.screens.auth.LegalScreen
import com.zevira.app.ui.screens.auth.LoginScreen
import com.zevira.app.ui.screens.auth.RegisterScreen
import com.zevira.app.ui.screens.compass.CompassScreen
import com.zevira.app.ui.screens.history.TripDetailScreen
import com.zevira.app.ui.screens.main.MainScreen
import com.zevira.app.ui.screens.map.TrackingLiveScreen
import com.zevira.app.ui.screens.onboarding.OnboardingScreen
import com.zevira.app.ui.screens.permission.PermissionPrimerScreen
import com.zevira.app.ui.screens.returntostart.ReturnToStartScreen
import com.zevira.app.ui.screens.summary.TripSummaryScreen

@Composable
fun ZeviraNavGraph(
    navController: NavHostController,
    startDestination: String = NavRoute.Main.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavRoute.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate(NavRoute.PermissionPrimer.route) {
                        popUpTo(NavRoute.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoute.PermissionPrimer.route) {
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            val prefs = remember { UserPreferencesDataStore(context) }
            PermissionPrimerScreen(
                onPermissionsGranted = {
                    coroutineScope.launch {
                        prefs.setOnboardingCompleted(true)
                    }
                    navController.navigate(NavRoute.Main.route) {
                        popUpTo(NavRoute.PermissionPrimer.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoute.Main.route) {
            MainScreen(navController = navController)
        }

        composable(NavRoute.TrackingLive.route) {
            TrackingLiveScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReturnToStart = { navController.navigate(NavRoute.ReturnToStart.route) },
                onNavigateToSummary = { tripId ->
                    navController.navigate(NavRoute.TripSummary.createRoute(tripId)) {
                        popUpTo(NavRoute.TrackingLive.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoute.ReturnToStart.route) {
            ReturnToStartScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoute.Compass.route) {
            CompassScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoute.TripSummary.route,
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            TripSummaryScreen(
                tripId = tripId,
                onNavigateHome = {
                    navController.navigate(NavRoute.Main.route) {
                        popUpTo(NavRoute.Main.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = NavRoute.TripDetail.route,
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            TripDetailScreen(
                tripId = tripId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoute.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(NavRoute.Register.route) },
                onLoginSuccess = { navController.popBackStack() }
            )
        }

        composable(NavRoute.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToTerms = { navController.navigate(NavRoute.Terms.route) },
                onNavigateToPrivacy = { navController.navigate(NavRoute.Privacy.route) },
                onRegisterSuccess = {
                    navController.navigate(NavRoute.Main.route) {
                        popUpTo(NavRoute.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoute.Terms.route) {
            LegalScreen(
                isTerms = true,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoute.Privacy.route) {
            LegalScreen(
                isTerms = false,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

package com.zevira.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.zevira.app.data.local.preferences.UserPreferencesDataStore
import com.zevira.app.ui.navigation.NavRoute
import com.zevira.app.ui.navigation.ZeviraNavGraph
import com.zevira.app.ui.theme.ZeviraTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesDataStore: UserPreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        // Native SplashScreen API (max 600ms)
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val onboardingCompleted by preferencesDataStore.onboardingCompletedFlow.collectAsState(initial = null)

            if (onboardingCompleted != null) {
                ZeviraTheme {
                    val navController = rememberNavController()
                    val startDest = if (onboardingCompleted == true) {
                        NavRoute.Main.route
                    } else {
                        NavRoute.Onboarding.route
                    }
                    ZeviraNavGraph(navController = navController, startDestination = startDest)
                }
            }
        }
    }
}

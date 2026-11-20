package com.zevira.app

import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
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
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        enforceLightSystemBarIcons()

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

    override fun onResume() {
        super.onResume()
        enforceLightSystemBarIcons()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enforceLightSystemBarIcons()
        }
    }

    private fun enforceLightSystemBarIcons() {
        @Suppress("DEPRECATION")
        window.statusBarColor = AndroidColor.TRANSPARENT
        @Suppress("DEPRECATION")
        window.navigationBarColor = AndroidColor.TRANSPARENT
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        }
    }
}

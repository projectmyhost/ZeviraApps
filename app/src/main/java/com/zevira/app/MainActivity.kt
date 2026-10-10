package com.zevira.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.zevira.app.ui.navigation.ZeviraNavGraph
import com.zevira.app.ui.theme.ZeviraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Native SplashScreen API (max 600ms)
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ZeviraTheme {
                val navController = rememberNavController()
                ZeviraNavGraph(navController = navController)
            }
        }
    }
}

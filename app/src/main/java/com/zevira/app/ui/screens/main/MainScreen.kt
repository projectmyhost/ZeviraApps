package com.zevira.app.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.zevira.app.ui.navigation.BottomTab
import com.zevira.app.ui.navigation.NavRoute
import com.zevira.app.ui.screens.dashboard.DashboardScreen
import com.zevira.app.ui.screens.history.HistoryScreen
import com.zevira.app.ui.screens.map.TrackingLiveScreen
import com.zevira.app.ui.screens.profile.ProfileScreen
import com.zevira.app.ui.theme.ElectricBlue
import com.zevira.app.ui.theme.StarYellow
import com.zevira.app.ui.theme.StickerBorder
import com.zevira.app.ui.theme.SurfaceWhite
import com.zevira.app.ui.theme.TextPrimary
import com.zevira.app.ui.theme.TextSecondary

@Composable
fun MainScreen(
    navController: NavController
) {
    var selectedTab by remember { mutableStateOf<BottomTab>(BottomTab.Home) }

    val tabs = listOf(
        BottomTab.Home,
        BottomTab.Map,
        BottomTab.History,
        BottomTab.Profile
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite)
                    .border(width = 2.dp, color = StickerBorder, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                NavigationBar(
                    containerColor = SurfaceWhite,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    tabs.forEach { tab ->
                        val isSelected = selectedTab == tab
                        val icon: ImageVector = when (tab) {
                            BottomTab.Home -> Icons.Rounded.Home
                            BottomTab.Map -> Icons.Rounded.Map
                            BottomTab.History -> Icons.Rounded.History
                            BottomTab.Profile -> Icons.Rounded.Person
                        }

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = stringResource(tab.titleResId),
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(tab.titleResId),
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextPrimary,
                                selectedTextColor = TextPrimary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = StarYellow
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            when (selectedTab) {
                BottomTab.Home -> {
                    DashboardScreen(
                        onNavigateToLiveTracking = {
                            selectedTab = BottomTab.Map
                        },
                        onNavigateToLogin = {
                            navController.navigate(NavRoute.Login.route)
                        },
                        onNavigateToCompass = {
                            navController.navigate(NavRoute.Compass.route)
                        },
                        onNavigateToTripDetail = { tripId ->
                            navController.navigate(NavRoute.TripDetail.createRoute(tripId))
                        }
                    )
                }
                BottomTab.Map -> {
                    TrackingLiveScreen(
                        onNavigateBack = {
                            selectedTab = BottomTab.Home
                        },
                        onNavigateToReturnToStart = {
                            navController.navigate(NavRoute.ReturnToStart.route)
                        },
                        onNavigateToSummary = { tripId ->
                            navController.navigate(NavRoute.TripSummary.createRoute(tripId))
                        }
                    )
                }
                BottomTab.History -> {
                    HistoryScreen(
                        onNavigateToDetail = { tripId ->
                            navController.navigate(NavRoute.TripDetail.createRoute(tripId))
                        }
                    )
                }
                BottomTab.Profile -> {
                    ProfileScreen(
                        onNavigateToLogin = {
                            navController.navigate(NavRoute.Login.route)
                        }
                    )
                }
            }
        }
    }
}

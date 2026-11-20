package com.zevira.app.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

private val NavBackdropBg = Color(0xFF0A0B0F)
private val NavDockSurface = Color(0xFF14151A)
private val NavActiveOrange = Color(0xFFF58216)
private val NavInactiveGray = Color(0xFF8C8F9C)

@Composable
fun MainScreen(
    navController: NavController
) {
    var selectedTab by remember { mutableStateOf<BottomTab>(BottomTab.Home) }

    val tabs = remember {
        listOf(
            BottomTab.Home,
            BottomTab.Map,
            BottomTab.History,
            BottomTab.Profile
        )
    }

    Scaffold(
        containerColor = NavBackdropBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            ZeviraFloatingBottomBar(
                tabs = tabs,
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it }
            )
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

@Composable
private fun ZeviraFloatingBottomBar(
    tabs: List<BottomTab>,
    selectedTab: BottomTab,
    onSelectTab: (BottomTab) -> Unit
) {
    val dockShape = RoundedCornerShape(32.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavBackdropBg)
            .navigationBarsPadding()
            .padding(horizontal = 8.dp)
            .padding(top = 4.dp, bottom = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .clip(dockShape)
                .background(NavDockSurface)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x29FFFFFF),
                            Color(0x0DFFFFFF)
                        )
                    ),
                    shape = dockShape
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                key(tab.route) {
                    val isSelected = selectedTab == tab
                    val icon: ImageVector = when (tab) {
                        BottomTab.Home -> Icons.Rounded.Home
                        BottomTab.Map -> Icons.Rounded.LocationOn
                        BottomTab.History -> Icons.Rounded.History
                        BottomTab.Profile -> Icons.Rounded.Person
                    }
                    ZeviraBottomNavItem(
                        label = stringResource(tab.titleResId),
                        icon = icon,
                        selected = isSelected,
                        onClick = { onSelectTab(tab) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun ZeviraBottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemColor = if (selected) NavActiveOrange else NavInactiveGray
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .then(
                if (selected) {
                    Modifier.background(
                        Brush.radialGradient(
                            colorStops = arrayOf(
                                0.00f to Color(0x33F57E14),
                                0.55f to Color(0x14F57E14),
                                1.00f to Color.Transparent
                            )
                        )
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = itemColor,
                modifier = Modifier.size(if (selected) 24.dp else 23.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                color = itemColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 11.5.sp,
                maxLines = 1
            )
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(40.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFE06410),
                                Color(0xFFFF9B2E),
                                Color(0xFFE06410)
                            )
                        )
                    )
            )
        }
    }
}

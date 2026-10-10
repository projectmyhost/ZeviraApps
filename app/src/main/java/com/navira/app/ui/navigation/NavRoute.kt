package com.navira.app.ui.navigation

sealed class NavRoute(val route: String) {
    object Onboarding : NavRoute("onboarding")
    object PermissionPrimer : NavRoute("permission_primer")
    object Main : NavRoute("main")
    object TrackingLive : NavRoute("tracking_live")
    object ReturnToStart : NavRoute("return_to_start")
    object Compass : NavRoute("compass")
    object TripSummary : NavRoute("trip_summary/{tripId}") {
        fun createRoute(tripId: String) = "trip_summary/$tripId"
    }
    object TripDetail : NavRoute("trip_detail/{tripId}") {
        fun createRoute(tripId: String) = "trip_detail/$tripId"
    }
    object Login : NavRoute("login")
    object Register : NavRoute("register")
    object Terms : NavRoute("terms")
    object Privacy : NavRoute("privacy")
}

sealed class BottomTab(val route: String, val titleResId: Int, val iconResId: String) {
    object Home : BottomTab("tab_home", com.navira.app.R.string.nav_home, "home")
    object Map : BottomTab("tab_map", com.navira.app.R.string.nav_map, "map")
    object History : BottomTab("tab_history", com.navira.app.R.string.nav_history, "history")
    object Profile : BottomTab("tab_profile", com.navira.app.R.string.nav_profile, "profile")
}

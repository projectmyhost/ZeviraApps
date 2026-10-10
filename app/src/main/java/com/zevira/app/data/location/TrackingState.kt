package com.zevira.app.data.location

import com.zevira.app.data.local.entity.SportActivity
import com.zevira.app.data.local.entity.TrackingMode

data class TrackingState(
    val tripId: String = "",
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val isSearchingGps: Boolean = true,
    val isGpsWeak: Boolean = false,
    val mode: TrackingMode = TrackingMode.WALK,
    val subActivity: SportActivity = SportActivity.NONE,
    val distanceMeters: Double = 0.0,
    val durationSeconds: Long = 0L,
    val movingDurationSeconds: Long = 0L,
    val currentSpeedKmh: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,
    val calories: Double = 0.0,
    val startLat: Double? = null,
    val startLng: Double? = null,
    val currentLat: Double? = null,
    val currentLng: Double? = null,
    val currentAccuracy: Float = 0f,
    val currentBearing: Float = 0f,
    val pointsCount: Int = 0
)

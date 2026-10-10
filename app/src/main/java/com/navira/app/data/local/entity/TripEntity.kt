package com.navira.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TrackingMode {
    WALK,
    SPORT
}

enum class SportActivity {
    NONE,
    WALK,
    RUN,
    CYCLE
}

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey
    val id: String, // UUID
    val ownerUid: String,
    val title: String,
    val mode: TrackingMode = TrackingMode.WALK,
    val subActivity: SportActivity = SportActivity.NONE,
    val startTime: Long,
    val endTime: Long = 0L,
    val distanceMeters: Double = 0.0,
    val durationSeconds: Long = 0L,
    val movingDurationSeconds: Long = 0L,
    val calories: Double = 0.0,
    val avgSpeedKmh: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,
    val startLat: Double? = null,
    val startLng: Double? = null,
    val endLat: Double? = null,
    val endLng: Double? = null,
    val encodedPolyline: String = "",
    val isCompleted: Boolean = false,
    val isSynced: Boolean = false,
    val isDemo: Boolean = false
)

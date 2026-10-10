package com.navira.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "location_points",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tripId"]), Index(value = ["timestamp"])]
)
data class LocationPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val tripId: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val speed: Float = 0f, // m/s
    val bearing: Float = 0f,
    val timestamp: Long,
    val isSegmentStart: Boolean = false
)

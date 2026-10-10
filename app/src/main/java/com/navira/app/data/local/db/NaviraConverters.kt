package com.navira.app.data.local.db

import androidx.room.TypeConverter
import com.navira.app.data.local.entity.SportActivity
import com.navira.app.data.local.entity.TrackingMode

class NaviraConverters {

    @TypeConverter
    fun fromTrackingMode(mode: TrackingMode): String = mode.name

    @TypeConverter
    fun toTrackingMode(name: String): TrackingMode = try {
        TrackingMode.valueOf(name)
    } catch (e: Exception) {
        TrackingMode.WALK
    }

    @TypeConverter
    fun fromSportActivity(activity: SportActivity): String = activity.name

    @TypeConverter
    fun toSportActivity(name: String): SportActivity = try {
        SportActivity.valueOf(name)
    } catch (e: Exception) {
        SportActivity.NONE
    }
}

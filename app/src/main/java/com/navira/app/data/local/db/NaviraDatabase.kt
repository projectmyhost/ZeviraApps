package com.navira.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.navira.app.data.local.entity.LocationPointEntity
import com.navira.app.data.local.entity.TripEntity

@Database(
    entities = [
        TripEntity::class,
        LocationPointEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(NaviraConverters::class)
abstract class NaviraDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun locationPointDao(): LocationPointDao
}

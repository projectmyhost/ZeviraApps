package com.zevira.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zevira.app.data.local.entity.LocationPointEntity
import com.zevira.app.data.local.entity.TripEntity

@Database(
    entities = [
        TripEntity::class,
        LocationPointEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(ZeviraConverters::class)
abstract class ZeviraDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun locationPointDao(): LocationPointDao
}

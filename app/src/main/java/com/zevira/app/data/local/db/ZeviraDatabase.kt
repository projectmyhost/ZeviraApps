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

    companion object {
        @Volatile
        private var INSTANCE: ZeviraDatabase? = null

        fun getInstance(context: android.content.Context): ZeviraDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    ZeviraDatabase::class.java,
                    "zevira_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

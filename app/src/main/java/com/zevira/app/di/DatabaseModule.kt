package com.zevira.app.di

import android.content.Context
import androidx.room.Room
import com.zevira.app.data.local.db.LocationPointDao
import com.zevira.app.data.local.db.ZeviraDatabase
import com.zevira.app.data.local.db.TripDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideZeviraDatabase(
        @ApplicationContext context: Context
    ): ZeviraDatabase {
        return Room.databaseBuilder(
            context,
            ZeviraDatabase::class.java,
            "zevira_database.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTripDao(database: ZeviraDatabase): TripDao {
        return database.tripDao()
    }

    @Provides
    fun provideLocationPointDao(database: ZeviraDatabase): LocationPointDao {
        return database.locationPointDao()
    }
}

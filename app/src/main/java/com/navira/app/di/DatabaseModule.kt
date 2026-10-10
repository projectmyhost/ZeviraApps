package com.navira.app.di

import android.content.Context
import androidx.room.Room
import com.navira.app.data.local.db.LocationPointDao
import com.navira.app.data.local.db.NaviraDatabase
import com.navira.app.data.local.db.TripDao
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
    fun provideNaviraDatabase(
        @ApplicationContext context: Context
    ): NaviraDatabase {
        return Room.databaseBuilder(
            context,
            NaviraDatabase::class.java,
            "navira_database.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTripDao(database: NaviraDatabase): TripDao {
        return database.tripDao()
    }

    @Provides
    fun provideLocationPointDao(database: NaviraDatabase): LocationPointDao {
        return database.locationPointDao()
    }
}

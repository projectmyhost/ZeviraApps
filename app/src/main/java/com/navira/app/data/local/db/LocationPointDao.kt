package com.navira.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.navira.app.data.local.entity.LocationPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationPointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoint(point: LocationPointEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoints(points: List<LocationPointEntity>)

    @Query("SELECT * FROM location_points WHERE tripId = :tripId ORDER BY timestamp ASC")
    suspend fun getPointsForTrip(tripId: String): List<LocationPointEntity>

    @Query("SELECT * FROM location_points WHERE tripId = :tripId ORDER BY timestamp ASC")
    fun observePointsForTrip(tripId: String): Flow<List<LocationPointEntity>>

    @Query("SELECT * FROM location_points WHERE tripId = :tripId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastPoint(tripId: String): LocationPointEntity?

    @Query("SELECT COUNT(*) FROM location_points WHERE tripId = :tripId")
    suspend fun getPointCount(tripId: String): Int

    @Query("DELETE FROM location_points WHERE tripId = :tripId")
    suspend fun deletePointsForTrip(tripId: String)
}

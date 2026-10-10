package com.zevira.app.data.local.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zevira.app.data.local.entity.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
    suspend fun getTripById(id: String): TripEntity?

    @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
    fun observeTripById(id: String): Flow<TripEntity?>

    @Query("SELECT * FROM trips WHERE isCompleted = 0 ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveUnfinishedTrip(): TripEntity?

    @Query("SELECT * FROM trips WHERE isCompleted = 0 ORDER BY startTime DESC LIMIT 1")
    fun observeActiveUnfinishedTrip(): Flow<TripEntity?>

    @Query("SELECT * FROM trips WHERE ownerUid = :ownerUid AND isCompleted = 1 ORDER BY startTime DESC")
    fun getTripsPaged(ownerUid: String): PagingSource<Int, TripEntity>

    @Query("SELECT * FROM trips WHERE ownerUid = :ownerUid AND isCompleted = 1 ORDER BY startTime DESC")
    fun observeCompletedTrips(ownerUid: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE ownerUid = :ownerUid AND isCompleted = 1 ORDER BY startTime DESC LIMIT 1")
    fun observeRecentTrip(ownerUid: String): Flow<TripEntity?>

    @Query("SELECT COUNT(*) FROM trips WHERE ownerUid = :ownerUid AND isDemo = 1")
    suspend fun countDemoTrips(ownerUid: String): Int

    @Query("SELECT * FROM trips WHERE isSynced = 0 AND isDemo = 0 AND isCompleted = 1")
    suspend fun getUnsyncedTrips(): List<TripEntity>

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteTripById(id: String)

    @Query("DELETE FROM trips WHERE ownerUid = :ownerUid")
    suspend fun deleteAllTripsForUser(ownerUid: String)

    @Query("UPDATE trips SET ownerUid = :newUid, isDemo = 0, isSynced = 0 WHERE ownerUid = :demoUid")
    suspend fun migrateDemoTripsToMember(demoUid: String, newUid: String)
}

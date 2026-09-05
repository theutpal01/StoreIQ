package com.storiq.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.storiq.core.model.StorageSnapshot
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageSnapshotDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(snapshot: StorageSnapshot): Long

    @Query("SELECT * FROM storage_snapshots ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(): StorageSnapshot?

    @Query("SELECT * FROM storage_snapshots ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<StorageSnapshot>

    @Query("SELECT * FROM storage_snapshots WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    suspend fun getRange(startTime: Long, endTime: Long): List<StorageSnapshot>

    @Query("SELECT * FROM storage_snapshots ORDER BY timestamp ASC")
    fun observeAll(): Flow<List<StorageSnapshot>>

    @Query("DELETE FROM storage_snapshots WHERE timestamp < :cutoffTime")
    suspend fun deleteOlderThan(cutoffTime: Long): Int

    @Transaction
    suspend fun insertAndTrim(snapshot: StorageSnapshot, maxEntries: Int = 1000) {
        insert(snapshot)
        val countQuery = "SELECT COUNT(*) FROM storage_snapshots"
        // We'll handle trimming in the repository
    }
}
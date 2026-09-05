package com.storiq.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.storiq.core.model.SwipeSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SwipeSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SwipeSession): Long

    @Update
    suspend fun update(session: SwipeSession): Int

    @Query("SELECT * FROM swipe_sessions ORDER BY startedAt DESC LIMIT 1")
    suspend fun getLatest(): SwipeSession?

    @Query("SELECT * FROM swipe_sessions WHERE mediaType = :mediaType ORDER BY startedAt DESC LIMIT 1")
    suspend fun getLatestByType(mediaType: com.storiq.core.model.SwipeMediaType): SwipeSession?

    @Query("SELECT * FROM swipe_sessions ORDER BY startedAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<SwipeSession>

    @Query("SELECT * FROM swipe_sessions WHERE id = :id")
    suspend fun getById(id: Long): SwipeSession?

    @Query("DELETE FROM swipe_sessions WHERE startedAt < :cutoffTime")
    suspend fun deleteOlderThan(cutoffTime: Long): Int
}
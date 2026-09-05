package com.storiq.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.storiq.core.model.ScanSession
import com.storiq.core.model.ScanStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ScanSession): Long

    @Update
    suspend fun update(session: ScanSession): Int

    @Query("SELECT * FROM scan_sessions ORDER BY startedAt DESC LIMIT 1")
    suspend fun getLatest(): ScanSession?

    @Query("SELECT * FROM scan_sessions WHERE status = :status ORDER BY startedAt DESC")
    suspend fun getByStatus(status: ScanStatus): List<ScanSession>

    @Query("SELECT * FROM scan_sessions ORDER BY startedAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<ScanSession>

    @Query("DELETE FROM scan_sessions WHERE startedAt < :cutoffTime")
    suspend fun deleteOlderThan(cutoffTime: Long): Int
}
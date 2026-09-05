package com.storiq.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.storiq.core.model.FileRecord
import com.storiq.core.model.MediaCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface FileRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<FileRecord>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: FileRecord): Long

    @Query("SELECT * FROM file_records WHERE category = :category ORDER BY sizeBytes DESC")
    suspend fun getByCategory(category: MediaCategory): List<FileRecord>

    @Query("SELECT * FROM file_records WHERE sizeBytes >= :minSize ORDER BY sizeBytes DESC")
    suspend fun getLargeFiles(minSize: Long): List<FileRecord>

    @Query("SELECT * FROM file_records WHERE scanSessionId = :sessionId")
    suspend fun getByScanSession(sessionId: Long): List<FileRecord>

    @Query("SELECT * FROM file_records WHERE hash IS NOT NULL AND hash != '' ORDER BY hash, sizeBytes DESC")
    suspend fun getAllWithHash(): List<FileRecord>

    @Query("DELETE FROM file_records WHERE scanSessionId = :sessionId")
    suspend fun deleteByScanSession(sessionId: Long): Int

    @Query("SELECT COUNT(*) FROM file_records")
    suspend fun getTotalCount(): Int

    @Query("SELECT SUM(sizeBytes) FROM file_records")
    suspend fun getTotalSize(): Long?

    @Query("UPDATE file_records SET hash = :hash WHERE id = :id")
    suspend fun updateHash(id: Long, hash: String): Int
}
package com.storiq.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.storiq.core.model.MediaRecord
import com.storiq.core.model.MediaCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<MediaRecord>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MediaRecord): Long

    @Query("SELECT * FROM media_records WHERE category = :category ORDER BY sizeBytes DESC")
    suspend fun getByCategory(category: MediaCategory): List<MediaRecord>

    @Query("SELECT * FROM media_records WHERE category = :category ORDER BY sizeBytes DESC LIMIT :limit OFFSET :offset")
    suspend fun getByCategoryPaged(category: MediaCategory, limit: Int, offset: Int): List<MediaRecord>

    @Query("SELECT * FROM media_records WHERE sizeBytes >= :minSize ORDER BY sizeBytes DESC")
    suspend fun getLargeFiles(minSize: Long): List<MediaRecord>

    @Query("SELECT * FROM media_records WHERE scanSessionId = :sessionId")
    suspend fun getByScanSession(sessionId: Long): List<MediaRecord>

    @Query("SELECT * FROM media_records WHERE isScreenshot = 1 ORDER BY modifiedDate DESC")
    suspend fun getScreenshots(): List<MediaRecord>

    @Query("SELECT * FROM media_records WHERE isScreenRecording = 1 ORDER BY modifiedDate DESC")
    suspend fun getScreenRecordings(): List<MediaRecord>

    @Query("SELECT * FROM media_records WHERE hash IS NOT NULL AND hash != '' ORDER BY hash, sizeBytes DESC")
    suspend fun getAllWithHash(): List<MediaRecord>

    @Query("SELECT * FROM media_records WHERE uri = :uri")
    suspend fun getByUri(uri: String): MediaRecord?

    @Query("DELETE FROM media_records WHERE scanSessionId = :sessionId")
    suspend fun deleteByScanSession(sessionId: Long): Int

    @Query("DELETE FROM media_records WHERE uri IN (:uris)")
    suspend fun deleteByUris(uris: List<String>): Int

    @Query("SELECT COUNT(*) FROM media_records")
    suspend fun getTotalCount(): Int

    @Query("SELECT SUM(sizeBytes) FROM media_records")
    suspend fun getTotalSize(): Long?

    @Query("SELECT SUM(sizeBytes) FROM media_records WHERE category = :category")
    suspend fun getTotalSizeByCategory(category: MediaCategory): Long?

    @Query("SELECT * FROM media_records ORDER BY modifiedDate DESC LIMIT :limit OFFSET :offset")
    suspend fun getRecent(limit: Int, offset: Int): List<MediaRecord>

    @Query("SELECT * FROM media_records ORDER BY modifiedDate DESC LIMIT :limit OFFSET :offset")
    fun observeRecent(limit: Int, offset: Int): Flow<List<MediaRecord>>

    @Query("UPDATE media_records SET hash = :hash WHERE id = :id")
    suspend fun updateHash(id: Long, hash: String): Int
}
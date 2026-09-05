package com.storiq.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.storiq.core.model.AppRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AppRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<AppRecord>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AppRecord): Long

    @Update
    suspend fun update(record: AppRecord): Int

    @Query("SELECT * FROM app_records ORDER BY totalBytes DESC")
    suspend fun getAllSortedBySize(): List<AppRecord>

    @Query("SELECT * FROM app_records WHERE isSystemApp = 0 ORDER BY totalBytes DESC")
    suspend fun getUserAppsSortedBySize(): List<AppRecord>

    @Query("SELECT * FROM app_records WHERE lastUsedTimestamp IS NOT NULL AND lastUsedTimestamp < :cutoffTime ORDER BY lastUsedTimestamp ASC")
    suspend fun getUnusedApps(cutoffTime: Long): List<AppRecord>

    @Query("SELECT * FROM app_records WHERE packageName = :packageName")
    suspend fun getByPackageName(packageName: String): AppRecord?

    @Query("SELECT * FROM app_records WHERE totalBytes >= :minSize ORDER BY totalBytes DESC")
    suspend fun getLargeApps(minSize: Long): List<AppRecord>

    @Query("DELETE FROM app_records")
    suspend fun deleteAll(): Int

    @Query("SELECT SUM(totalBytes) FROM app_records WHERE isSystemApp = 0")
    suspend fun getTotalUserAppSize(): Long?

    @Query("SELECT SUM(totalBytes) FROM app_records")
    suspend fun getTotalAppSize(): Long?

    @Query("SELECT COUNT(*) FROM app_records WHERE isSystemApp = 0")
    suspend fun getUserAppCount(): Int
}
package com.storiq.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.storiq.core.model.DuplicateGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface DuplicateGroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(groups: List<DuplicateGroup>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: DuplicateGroup): Long

    @Query("SELECT * FROM duplicate_groups ORDER BY totalWastedBytes DESC")
    suspend fun getAllSortedByWastedSpace(): List<DuplicateGroup>

    @Query("SELECT * FROM duplicate_groups WHERE category = :category ORDER BY totalWastedBytes DESC")
    suspend fun getByCategory(category: com.storiq.core.model.MediaCategory): List<DuplicateGroup>

    @Query("SELECT * FROM duplicate_groups WHERE scanSessionId = :sessionId")
    suspend fun getByScanSession(sessionId: Long): List<DuplicateGroup>

    @Query("SELECT SUM(totalWastedBytes) FROM duplicate_groups")
    suspend fun getTotalWastedSpace(): Long?

    @Query("SELECT COUNT(*) FROM duplicate_groups")
    suspend fun getTotalGroupCount(): Int

    @Query("SELECT SUM(count) FROM duplicate_groups")
    suspend fun getTotalDuplicateFileCount(): Int

    @Query("DELETE FROM duplicate_groups WHERE scanSessionId = :sessionId")
    suspend fun deleteByScanSession(sessionId: Long): Int

    @Query("DELETE FROM duplicate_groups")
    suspend fun deleteAll(): Int
}
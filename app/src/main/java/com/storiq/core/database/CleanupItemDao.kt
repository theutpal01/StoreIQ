package com.storiq.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.storiq.core.model.CleanupItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CleanupItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CleanupItem>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CleanupItem): Long

    @Update
    suspend fun update(item: CleanupItem): Int

    @Query("SELECT * FROM cleanup_items ORDER BY estimatedBytes DESC")
    suspend fun getAllSortedBySize(): List<CleanupItem>

    @Query("SELECT * FROM cleanup_items WHERE category = :category ORDER BY estimatedBytes DESC")
    suspend fun getByCategory(category: com.storiq.core.model.CleanupCategory): List<CleanupItem>

    @Query("SELECT * FROM cleanup_items WHERE isSelected = 1")
    suspend fun getSelected(): List<CleanupItem>

    @Query("SELECT SUM(estimatedBytes) FROM cleanup_items WHERE isSelected = 1")
    suspend fun getSelectedTotalBytes(): Long?

    @Query("SELECT COUNT(*) FROM cleanup_items WHERE isSelected = 1")
    suspend fun getSelectedCount(): Int

    @Query("DELETE FROM cleanup_items")
    suspend fun deleteAll(): Int
}
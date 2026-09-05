package com.storiq.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.storiq.core.model.Recommendation
import kotlinx.coroutines.flow.Flow

@Dao
interface RecommendationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recommendations: List<Recommendation>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recommendation: Recommendation): Long

    @Update
    suspend fun update(recommendation: Recommendation): Int

    @Query("SELECT * FROM recommendations WHERE isDismissed = 0 ORDER BY priority DESC, estimatedRecoverableBytes DESC")
    suspend fun getActive(): List<Recommendation>

    @Query("SELECT * FROM recommendations WHERE type = :type AND isDismissed = 0")
    suspend fun getByType(type: com.storiq.core.model.RecommendationType): Recommendation?

    @Query("UPDATE recommendations SET isDismissed = 1 WHERE id = :id")
    suspend fun dismiss(id: Long): Int

    @Query("DELETE FROM recommendations")
    suspend fun deleteAll(): Int
}
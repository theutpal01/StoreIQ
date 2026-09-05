package com.storiq.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.storiq.core.model.SwipeDecisionRecord
import com.storiq.core.model.SwipeDecision
import kotlinx.coroutines.flow.Flow

@Dao
interface SwipeDecisionRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<SwipeDecisionRecord>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SwipeDecisionRecord): Long

    @Query("SELECT * FROM swipe_decision_records WHERE sessionId = :sessionId")
    suspend fun getBySessionId(sessionId: Long): List<SwipeDecisionRecord>

    @Query("SELECT * FROM swipe_decision_records WHERE sessionId = :sessionId AND decision = :decision")
    suspend fun getBySessionAndDecision(sessionId: Long, decision: SwipeDecision): List<SwipeDecisionRecord>

    @Query("SELECT COUNT(*) FROM swipe_decision_records WHERE sessionId = :sessionId AND decision = :decision")
    suspend fun getCountBySessionAndDecision(sessionId: Long, decision: SwipeDecision): Int

    @Query("SELECT * FROM swipe_decision_records WHERE mediaUri = :mediaUri")
    suspend fun getByMediaUri(mediaUri: String): List<SwipeDecisionRecord>

    @Query("DELETE FROM swipe_decision_records WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: Long): Int
}
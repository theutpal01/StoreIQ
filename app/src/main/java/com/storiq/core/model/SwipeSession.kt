package com.storiq.core.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class SwipeDecision {
    KEEP,
    DELETE,
    UNDECIDED
}

@Serializable
enum class SwipeMediaType {
    PHOTOS,
    VIDEOS,
    PHOTOS_AND_VIDEOS,
    SMART_CLEAN
}

@Serializable
@Entity(
    tableName = "swipe_sessions",
    indices = [Index(value = ["mediaType"]), Index(value = ["startedAt"])]
)
data class SwipeSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val mediaType: SwipeMediaType,
    val totalItems: Int = 0,
    val keptItems: Int = 0,
    val deleteCandidates: Int = 0,
    val totalCandidateBytes: Long = 0
) {
    val isCompleted: Boolean
        get() = completedAt != null
}

@Serializable
@Entity(
    tableName = "swipe_decision_records",
    indices = [Index(value = ["sessionId"]), Index(value = ["mediaUri"]), Index(value = ["decision"])]
)
data class SwipeDecisionRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val mediaUri: String,
    val decision: SwipeDecision,
    val timestamp: Long = System.currentTimeMillis()
)
package com.storiq.core.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class RecommendationPriority {
    URGENT,
    HIGH,
    MEDIUM,
    LOW,
    INFO
}

@Serializable
@Entity(
    tableName = "recommendations",
    indices = [Index(value = ["priority"]), Index(value = ["type"])]
)
data class Recommendation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: RecommendationType,
    val priority: RecommendationPriority,
    val title: String,
    val description: String,
    val estimatedRecoverableBytes: Long,
    val actionLabel: String,
    val actionRoute: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isDismissed: Boolean = false
) {
    val formattedRecoverableSize: String
        get() = StorageBreakdown.formatBytes(estimatedRecoverableBytes)
}

@Serializable
enum class RecommendationType {
    HIGH_STORAGE_USAGE,
    DUPLICATES_FOUND,
    LARGE_FILES_FOUND,
    UNUSED_APPS_FOUND,
    SCREENSHOTS_FOUND,
    OLD_DOWNLOADS_FOUND,
    STORAGE_TREND_WARNING
}
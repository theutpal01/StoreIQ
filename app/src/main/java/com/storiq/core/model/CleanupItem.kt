package com.storiq.core.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class CleanupCategory {
    DUPLICATES,
    OLD_APKS,
    EMPTY_FOLDERS,
    TEMPORARY_FILES,
    OLD_DOWNLOADS,
    SCREENSHOTS,
    LARGE_FILES,
    MEDIA
}

@Serializable
enum class CleanupRisk {
    LOW,
    MEDIUM,
    HIGH
}

@Serializable
@Entity(
    tableName = "cleanup_items",
    indices = [Index(value = ["category"]), Index(value = ["risk"])]
)
data class CleanupItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: CleanupCategory,
    val title: String,
    val description: String,
    val whyRecommended: String,
    val whatHappensIfDeleted: String,
    val estimatedBytes: Long,
    val risk: CleanupRisk,
    val relatedUris: List<String>,
    val isSelected: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val formattedSize: String
        get() = StorageBreakdown.formatBytes(estimatedBytes)
}
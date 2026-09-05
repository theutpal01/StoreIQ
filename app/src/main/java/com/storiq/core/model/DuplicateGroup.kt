package com.storiq.core.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "duplicate_groups",
    indices = [Index(value = ["hash"]), Index(value = ["sizeBytes"])]
)
data class DuplicateGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hash: String,
    val sizeBytes: Long,
    val count: Int,
    val category: MediaCategory,
    val recommendedKeepUri: String?,
    val scanSessionId: Long,
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalWastedBytes: Long
        get() = sizeBytes * (count - 1)

    val formattedWastedSize: String
        get() = StorageBreakdown.formatBytes(totalWastedBytes)
}
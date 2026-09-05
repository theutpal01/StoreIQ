package com.storiq.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "storage_snapshots")
data class StorageSnapshot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val appBytes: Long,
    val mediaBytes: Long,
    val documentsBytes: Long,
    val otherBytes: Long
) {
    val usagePercent: Float
        get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes) * 100 else 0f
}
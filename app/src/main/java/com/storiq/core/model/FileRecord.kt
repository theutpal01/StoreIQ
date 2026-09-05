package com.storiq.core.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "file_records",
    indices = [
        Index(value = ["category"]),
        Index(value = ["sizeBytes"]),
        Index(value = ["modifiedDate"]),
        Index(value = ["hash"])
    ]
)
data class FileRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: String,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val modifiedDate: Long,
    val createdDate: Long,
    val relativePath: String?,
    val hash: String?,
    val category: MediaCategory,
    val scanSessionId: Long
) {
    val formattedSize: String
        get() = StorageBreakdown.formatBytes(sizeBytes)
}
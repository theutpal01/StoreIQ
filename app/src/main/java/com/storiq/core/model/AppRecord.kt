package com.storiq.core.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "app_records",
    indices = [
        Index(value = ["packageName"], unique = true),
        Index(value = ["totalBytes"]),
        Index(value = ["lastUsedTimestamp"])
    ]
)
data class AppRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val totalBytes: Long,
    val appBytes: Long,
    val userDataBytes: Long,
    val cacheBytes: Long,
    val installDate: Long,
    val lastUsedTimestamp: Long?,
    val isSystemApp: Boolean = false,
    val versionName: String?,
    val versionCode: Int?
) {
    val formattedTotalSize: String
        get() = StorageBreakdown.formatBytes(totalBytes)

    val formattedAppSize: String
        get() = StorageBreakdown.formatBytes(appBytes)

    val formattedUserDataSize: String
        get() = StorageBreakdown.formatBytes(userDataBytes)

    val isUnused: Boolean
        get() = lastUsedTimestamp == null || (System.currentTimeMillis() - lastUsedTimestamp!!) > 90 * 24 * 60 * 60 * 1000L

    val daysSinceLastUsed: Int?
        get() = lastUsedTimestamp?.let {
            ((System.currentTimeMillis() - it) / (24 * 60 * 60 * 1000L)).toInt()
        }
}
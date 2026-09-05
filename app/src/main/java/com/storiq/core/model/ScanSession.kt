package com.storiq.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class ScanStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED
}

@Serializable
@Entity(tableName = "scan_sessions")
data class ScanSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val status: ScanStatus = ScanStatus.PENDING,
    val totalFilesScanned: Int = 0,
    val totalMediaScanned: Int = 0,
    val totalAppsScanned: Int = 0,
    val totalBytesScanned: Long = 0,
    val errorMessage: String? = null,
    val scanType: ScanType = ScanType.FULL
)

@Serializable
enum class ScanType {
    FULL,
    MEDIA_ONLY,
    APPS_ONLY,
    DOWNLOADS_ONLY,
    INCREMENTAL
}
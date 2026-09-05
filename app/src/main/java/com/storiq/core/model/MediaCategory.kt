package com.storiq.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class MediaCategory {
    IMAGES,
    VIDEOS,
    AUDIO,
    DOCUMENTS,
    DOWNLOADS,
    ARCHIVES,
    APKS,
    OTHER,
    SCREENSHOTS,
    SCREEN_RECORDINGS
}

@Serializable
data class StorageBreakdown(
    val category: MediaCategory,
    val bytes: Long,
    val count: Int,
    val isEstimated: Boolean = false
) {
    val formattedSize: String
        get() = formatBytes(bytes)

    companion object {
        fun formatBytes(bytes: Long): String {
            return when {
                bytes >= 1_099_511_627_776L -> "%.2f TB".format(bytes / 1_099_511_627_776.0)
                bytes >= 1_073_741_824L -> "%.2f GB".format(bytes / 1_073_741_824.0)
                bytes >= 1_048_576L -> "%.2f MB".format(bytes / 1_048_576.0)
                bytes >= 1024L -> "%.2f KB".format(bytes / 1024.0)
                else -> "$bytes B"
            }
        }
    }
}
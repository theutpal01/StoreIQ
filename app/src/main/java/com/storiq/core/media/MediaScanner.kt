package com.storiq.core.media

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.storiq.core.model.MediaCategory
import com.storiq.core.model.MediaRecord
import com.storiq.core.storage.StorageRepository
import com.storiq.core.util.CoroutineDispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean

class MediaScanner(
    private val context: Context,
    private val repository: StorageRepository,
    private val dispatcherProvider: CoroutineDispatcherProvider
) {

    private val isScanning = AtomicBoolean(false)
    private val contentResolver: ContentResolver = context.contentResolver

    suspend fun scanMedia(scanSessionId: Long): ScanResult {
        return withContext(dispatcherProvider.io) {
            if (isScanning.getAndSet(true)) {
                return@withContext ScanResult(success = false, errorMessage = "Scan already in progress")
            }

            var totalScanned = 0
            var totalSize = 0L
            val errors = mutableListOf<String>()

            try {
                // Scan images
                val imageResult = scanImages(scanSessionId)
                totalScanned += imageResult.count
                totalSize += imageResult.totalSize
                errors.addAll(imageResult.errors)

                // Scan videos
                val videoResult = scanVideos(scanSessionId)
                totalScanned += videoResult.count
                totalSize += videoResult.totalSize
                errors.addAll(videoResult.errors)

                // Scan audio
                val audioResult = scanAudio(scanSessionId)
                totalScanned += audioResult.count
                totalSize += audioResult.totalSize
                errors.addAll(audioResult.errors)

                ScanResult(
                    success = true,
                    totalScanned = totalScanned,
                    totalSize = totalSize,
                    errors = errors
                )
            } catch (e: Exception) {
                ScanResult(success = false, errorMessage = e.message)
            } finally {
                isScanning.set(false)
            }
        }
    }

    private fun scanImages(scanSessionId: Long): MediaScanResult {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.RELATIVE_PATH,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.DATA
        )

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        return scanMediaStore(uri, projection, scanSessionId) { cursor ->
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
            val contentUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
            val displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
            val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE))
            val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
            val modifiedDate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)) * 1000L
            val createdDate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)) * 1000L
            val relativePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH))
            val width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH))
            val height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT))

            val category = when {
                isScreenshot(displayName, relativePath) -> MediaCategory.SCREENSHOTS
                else -> MediaCategory.IMAGES
            }

            MediaRecord(
                uri = contentUri.toString(),
                displayName = displayName,
                mimeType = mimeType,
                sizeBytes = size,
                modifiedDate = modifiedDate,
                createdDate = createdDate,
                relativePath = relativePath,
                width = width,
                height = height,
                durationMs = null,
                hash = null, // Computed later for duplicates
                category = category,
                isScreenshot = category == MediaCategory.SCREENSHOTS,
                isScreenRecording = false,
                scanSessionId = scanSessionId
            )
        }
    }

    private fun scanVideos(scanSessionId: Long): MediaScanResult {
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.RELATIVE_PATH,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATA
        )

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        return scanMediaStore(uri, projection, scanSessionId) { cursor ->
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
            val contentUri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString())
            val displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))
            val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE))
            val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))
            val modifiedDate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)) * 1000L
            val createdDate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)) * 1000L
            val relativePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH))
            val width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH))
            val height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT))
            val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))

            val category = when {
                isScreenRecording(displayName, relativePath) -> MediaCategory.SCREEN_RECORDINGS
                else -> MediaCategory.VIDEOS
            }

            MediaRecord(
                uri = contentUri.toString(),
                displayName = displayName,
                mimeType = mimeType,
                sizeBytes = size,
                modifiedDate = modifiedDate,
                createdDate = createdDate,
                relativePath = relativePath,
                width = width,
                height = height,
                durationMs = duration,
                hash = null,
                category = category,
                isScreenshot = false,
                isScreenRecording = category == MediaCategory.SCREEN_RECORDINGS,
                scanSessionId = scanSessionId
            )
        }
    }

    private fun scanAudio(scanSessionId: Long): MediaScanResult {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.RELATIVE_PATH,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        return scanMediaStore(uri, projection, scanSessionId) { cursor ->
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
            val contentUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())
            val displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
            val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE))
            val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
            val modifiedDate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)) * 1000L
            val createdDate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)) * 1000L
            val relativePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH))
            val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))

            MediaRecord(
                uri = contentUri.toString(),
                displayName = displayName,
                mimeType = mimeType,
                sizeBytes = size,
                modifiedDate = modifiedDate,
                createdDate = createdDate,
                relativePath = relativePath,
                width = null,
                height = null,
                durationMs = duration,
                hash = null,
                category = MediaCategory.AUDIO,
                isScreenshot = false,
                isScreenRecording = false,
                scanSessionId = scanSessionId
            )
        }
    }

    private fun scanMediaStore(
        uri: Uri,
        projection: Array<String>,
        scanSessionId: Long,
        mapCursor: (Cursor) -> MediaRecord
    ): MediaScanResult {
        var count = 0
        var totalSize = 0L
        val errors = mutableListOf<String>()
        val batchSize = 100
        val records = mutableListOf<MediaRecord>()

        try {
            contentResolver.query(uri, projection, null, null, "${MediaStore.MediaColumns.DATE_MODIFIED} DESC")?.use { cursor ->
                while (cursor.moveToNext()) {
                    try {
                        val record = mapCursor(cursor)
                        records.add(record)
                        count++
                        totalSize += record.sizeBytes

                        if (records.size >= batchSize) {
                            repository.database.mediaRecordDao().insertAll(records)
                            records.clear()
                        }
                    } catch (e: Exception) {
                        errors.add("Failed to parse media: ${e.message}")
                    }
                }
            }

            if (records.isNotEmpty()) {
                repository.database.mediaRecordDao().insertAll(records)
            }
        } catch (e: Exception) {
            errors.add("MediaStore query failed: ${e.message}")
        }

        return MediaScanResult(count, totalSize, errors)
    }

    private fun isScreenshot(displayName: String?, relativePath: String?): Boolean {
        val name = displayName?.lowercase() ?: ""
        val path = relativePath?.lowercase() ?: ""
        return name.contains("screenshot") ||
               name.contains("screen_shot") ||
               name.contains("screencapture") ||
               path.contains("screenshots") ||
               path.contains("screenshot")
    }

    private fun isScreenRecording(displayName: String?, relativePath: String?): Boolean {
        val name = displayName?.lowercase() ?: ""
        val path = relativePath?.lowercase() ?: ""
        return name.contains("screen_recording") ||
               name.contains("screenrecording") ||
               name.contains("screen_record") ||
               path.contains("screenrecord") ||
               path.contains("recordings")
    }

    suspend fun computeHashesForDuplicates(scanSessionId: Long): Int {
        return withContext(dispatcherProvider.io) {
            val records = repository.database.mediaRecordDao().getAllWithHash()
            var hashedCount = 0

            for (record in records) {
                if (record.hash == null || record.hash.isBlank()) {
                    val hash = computeFileHash(record.uri)
                    if (hash != null) {
                        repository.database.mediaRecordDao().updateHash(record.id, hash)
                        hashedCount++
                    }
                }
            }
            hashedCount
        }
    }

    private fun computeFileHash(uriString: String): String? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val digest = MessageDigest.getInstance("SHA-256")
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (stream.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
                digest.digest().joinToString("") { "%02x".format(it) }
            }
        } catch (e: Exception) {
            null
        }
    }

    data class MediaScanResult(
        val count: Int,
        val totalSize: Long,
        val errors: List<String>
    )

    data class ScanResult(
        val success: Boolean,
        val totalScanned: Int = 0,
        val totalSize: Long = 0L,
        val errors: List<String> = emptyList(),
        val errorMessage: String? = null
    )
}
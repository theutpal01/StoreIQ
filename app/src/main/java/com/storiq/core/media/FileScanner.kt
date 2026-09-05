package com.storiq.core.media

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.storiq.core.model.FileRecord
import com.storiq.core.model.MediaCategory
import com.storiq.core.storage.StorageRepository
import com.storiq.core.util.CoroutineDispatcherProvider
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean

class FileScanner(
    private val context: Context,
    private val repository: StorageRepository,
    private val dispatcherProvider: CoroutineDispatcherProvider
) {

    private val isScanning = AtomicBoolean(false)
    private val contentResolver: ContentResolver = context.contentResolver

    suspend fun scanDownloads(scanSessionId: Long): ScanResult {
        return withContext(dispatcherProvider.io) {
            if (isScanning.getAndSet(true)) {
                return@withContext ScanResult(success = false, errorMessage = "Scan already in progress")
            }

            var totalScanned = 0
            var totalSize = 0L
            val errors = mutableListOf<String>()
            val batchSize = 100
            val records = mutableListOf<FileRecord>()

            try {
                // Scan Downloads folder using MediaStore
                val downloadsResult = scanDownloadsMediaStore(scanSessionId)
                totalScanned += downloadsResult.count
                totalSize += downloadsResult.totalSize
                errors.addAll(downloadsResult.errors)

                // Scan Documents using DocumentsContract
                val documentsResult = scanDocuments(scanSessionId)
                totalScanned += documentsResult.count
                totalSize += documentsResult.totalSize
                errors.addAll(documentsResult.errors)

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

    private fun scanDownloadsMediaStore(scanSessionId: Long): MediaScanResult {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.RELATIVE_PATH,
            MediaStore.Files.FileColumns.DATA
        )

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val selection = "${MediaStore.Files.FileColumns.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("%Download%")

        return scanMediaStore(uri, projection, selection, selectionArgs, scanSessionId) { cursor ->
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
            val contentUri = Uri.withAppendedPath(MediaStore.Files.getContentUri("external"), id.toString())
            val displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME))
            val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE))
            val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE))
            val modifiedDate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)) * 1000L
            val createdDate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)) * 1000L
            val relativePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.RELATIVE_PATH))

            val category = categorizeFile(displayName, mimeType)

            FileRecord(
                uri = contentUri.toString(),
                displayName = displayName,
                mimeType = mimeType,
                sizeBytes = size,
                modifiedDate = modifiedDate,
                createdDate = createdDate,
                relativePath = relativePath,
                hash = null,
                category = category,
                scanSessionId = scanSessionId
            )
        }
    }

    private fun scanDocuments(scanSessionId: Long): MediaScanResult {
        var count = 0
        var totalSize = 0L
        val errors = mutableListOf<String>()
        val records = mutableListOf<FileRecord>()

        try {
            // Use DocumentsContract to access documents
            val uri = DocumentsContract.buildRootsUri(DocumentsContract.AUTHORITY)
            // This is simplified - in production you'd query for document roots
            // and then traverse the document tree
        } catch (e: Exception) {
            errors.add("Documents scan failed: ${e.message}")
        }

        return MediaScanResult(count, totalSize, errors)
    }

    private fun categorizeFile(displayName: String?, mimeType: String?): MediaCategory {
        val name = displayName?.lowercase() ?: ""
        val mime = mimeType?.lowercase() ?: ""

        return when {
            mime.startsWith("application/pdf") -> MediaCategory.DOCUMENTS
            mime.startsWith("application/vnd.openxmlformats") -> MediaCategory.DOCUMENTS
            mime.startsWith("application/msword") -> MediaCategory.DOCUMENTS
            mime.startsWith("text/") -> MediaCategory.DOCUMENTS
            mime.startsWith("application/zip") -> MediaCategory.ARCHIVES
            mime.startsWith("application/x-rar") -> MediaCategory.ARCHIVES
            mime.startsWith("application/x-7z") -> MediaCategory.ARCHIVES
            mime.startsWith("application/vnd.android.package-archive") -> MediaCategory.APKS
            mime.startsWith("video/") -> MediaCategory.VIDEOS
            mime.startsWith("image/") -> MediaCategory.IMAGES
            mime.startsWith("audio/") -> MediaCategory.AUDIO
            name.endsWith(".apk") -> MediaCategory.APKS
            else -> MediaCategory.OTHER
        }
    }

    private fun scanMediaStore(
        uri: Uri,
        projection: Array<String>,
        selection: String?,
        selectionArgs: Array<String>?,
        scanSessionId: Long,
        mapCursor: (Cursor) -> FileRecord
    ): MediaScanResult {
        var count = 0
        var totalSize = 0L
        val errors = mutableListOf<String>()
        val batchSize = 100
        val records = mutableListOf<FileRecord>()

        try {
            contentResolver.query(uri, projection, selection, selectionArgs, "${MediaStore.MediaColumns.DATE_MODIFIED} DESC")?.use { cursor ->
                while (cursor.moveToNext()) {
                    try {
                        val record = mapCursor(cursor)
                        records.add(record)
                        count++
                        totalSize += record.sizeBytes

                        if (records.size >= batchSize) {
                            repository.database.fileRecordDao().insertAll(records)
                            records.clear()
                        }
                    } catch (e: Exception) {
                        errors.add("Failed to parse file: ${e.message}")
                    }
                }
            }

            if (records.isNotEmpty()) {
                repository.database.fileRecordDao().insertAll(records)
            }
        } catch (e: Exception) {
            errors.add("MediaStore query failed: ${e.message}")
        }

        return MediaScanResult(count, totalSize, errors)
    }

    suspend fun computeHashesForDuplicates(): Int {
        return withContext(dispatcherProvider.io) {
            val records = repository.database.fileRecordDao().getAllWithHash()
            var hashedCount = 0

            for (record in records) {
                if (record.hash == null || record.hash.isBlank()) {
                    val hash = computeFileHash(record.uri)
                    if (hash != null) {
                        repository.database.fileRecordDao().updateHash(record.id, hash)
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
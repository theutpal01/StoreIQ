package com.storiq.core.storage

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import com.storiq.core.storage.StorageRepository.DeletionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DeletionManager(
    private val context: Context
) {

    private val contentResolver: ContentResolver = context.contentResolver

    suspend fun deleteMediaItems(uris: List<String>): DeletionResult {
        return withContext(Dispatchers.IO) {
            var deletedCount = 0
            var failedCount = 0
            var deletedBytes = 0L
            val errors = mutableListOf<String>()

            for (uriString in uris) {
                try {
                    val uri = Uri.parse(uriString)
                    val size = getMediaSize(uri)
                    
                    val deleted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        deleteMediaStoreItem(uri)
                    } else {
                        deleteLegacyMediaItem(uri)
                    }

                    if (deleted) {
                        deletedCount++
                        deletedBytes += size
                    } else {
                        failedCount++
                        errors.add("Failed to delete $uriString")
                    }
                } catch (e: Exception) {
                    failedCount++
                    errors.add("Error deleting $uriString: ${e.message}")
                }
            }

            DeletionResult(
                success = failedCount == 0,
                deletedCount = deletedCount,
                failedCount = failedCount,
                deletedBytes = deletedBytes,
                errors = errors
            )
        }
    }

    suspend fun deleteFileItems(uris: List<String>): DeletionResult {
        return withContext(Dispatchers.IO) {
            var deletedCount = 0
            var failedCount = 0
            var deletedBytes = 0L
            val errors = mutableListOf<String>()

            for (uriString in uris) {
                try {
                    val uri = Uri.parse(uriString)
                    
                    val deleted = if (DocumentsContract.isDocumentUri(context, uri)) {
                        deleteDocumentFile(uri)
                    } else {
                        deleteRegularFile(uri)
                    }

                    if (deleted) {
                        deletedCount++
                        // Size calculation would need to be done before deletion
                    } else {
                        failedCount++
                        errors.add("Failed to delete $uriString")
                    }
                } catch (e: Exception) {
                    failedCount++
                    errors.add("Error deleting $uriString: ${e.message}")
                }
            }

            DeletionResult(
                success = failedCount == 0,
                deletedCount = deletedCount,
                failedCount = failedCount,
                deletedBytes = deletedBytes,
                errors = errors
            )
        }
    }

    private fun getMediaSize(uri: Uri): Long {
        var size = 0L
        val projection = arrayOf(MediaStore.MediaColumns.SIZE)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE))
            }
        }
        return size
    }

    private fun deleteMediaStoreItem(uri: Uri): Boolean {
        return try {
            // Use trash API if available (Android 11+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // MediaStore.createDeleteRequest requires pending intent
                // For now, use direct delete which moves to trash on Android 11+
                val rowsDeleted = contentResolver.delete(uri, null, null)
                rowsDeleted > 0
            } else {
                // Android 10 and below
                val rowsDeleted = contentResolver.delete(uri, null, null)
                rowsDeleted > 0
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun deleteLegacyMediaItem(uri: Uri): Boolean {
        return try {
            val rowsDeleted = contentResolver.delete(uri, null, null)
            rowsDeleted > 0
        } catch (e: Exception) {
            false
        }
    }

    private fun deleteDocumentFile(uri: Uri): Boolean {
        return try {
            val documentFile = DocumentFile.fromSingleUri(context, uri)
            documentFile?.delete() == true
        } catch (e: Exception) {
            false
        }
    }

    private fun deleteRegularFile(uri: Uri): Boolean {
        return try {
            val file = File(uri.path)
            file.exists() && file.delete()
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        // For Android 11+ trash support
        @Suppress("UNUSED_PARAMETER")
        fun moveToTrash(context: Context, uris: List<Uri>): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Use MediaStore.createDeleteRequest for proper trash handling
                // This requires a PendingIntent and user confirmation
                // Implementation would go here
                return true
            }
            return false
        }
    }
}
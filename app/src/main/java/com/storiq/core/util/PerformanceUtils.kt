package com.storiq.core.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.WorkerThread
import com.storiq.core.util.CoroutineDispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

object PerformanceUtils {

    @WorkerThread
    fun loadThumbnail(
        context: android.content.Context,
        uri: Uri,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            if (options.outWidth == 0 || options.outHeight == 0) return null

            val scale = calculateInSampleSize(options, targetWidth, targetHeight)
            options.inJustDecodeBounds = false
            options.inSampleSize = scale
            options.inPreferredConfig = Bitmap.Config.RGB_565

            context.contentResolver.openInputStream(uri)?.use { input ->
                var bitmap = BitmapFactory.decodeStream(input, null, options)
                bitmap = rotateBitmapIfNeeded(bitmap, uri, context)
                if (bitmap != null && (bitmap.width > targetWidth || bitmap.height > targetHeight)) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                }
                bitmap
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            val halfHeight = options.outHeight / 2
            val halfWidth = options.outWidth / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap?, uri: Uri, context: android.content.Context): Bitmap? {
        bitmap?.let { bmp ->
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.use { stream ->
                    val exif = ExifInterface(stream)
                    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    val matrix = Matrix()
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    }
                    if (orientation != ExifInterface.ORIENTATION_NORMAL) {
                        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore rotation errors
        }
        bitmap
    }

    suspend fun computeFileHash(
        uri: Uri,
        context: android.content.Context,
        dispatcherProvider: CoroutineDispatcherProvider
    ): String? {
        return withContext(dispatcherProvider.io) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.use { stream ->
                    val digest = java.security.MessageDigest.getInstance("SHA-256")
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
    }

    @WorkerThread
    fun getVideoDimensions(uri: Uri, context: android.content.Context): Size? {
        return try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val width = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
            val height = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
            retriever.release()
            if (width != null && height != null) Size(width, height) else null
        } catch (e: Exception) {
            null
        }
    }

    fun calculateOptimalImageSize(
        imageWidth: Int,
        imageHeight: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Pair<Int, Int> {
        var width = imageWidth.toFloat()
        var height = imageHeight.toFloat()

        val widthRatio = maxWidth.toFloat() / width
        val heightRatio = maxHeight.toFloat() / height
        val ratio = minOf(widthRatio, heightRatio)

        if (ratio < 1f) {
            width *= ratio
            height *= ratio
        }

        return Pair(width.toInt(), height.toInt())
    }
}
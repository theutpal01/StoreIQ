package com.storiq.core.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.provider.MediaStore
import androidx.annotation.RequiresApi

object DeviceCompatibility {

    data class CompatibilityReport(
        val apiLevel: Int,
        val manufacturer: String,
        val model: String,
        val supportsScopedStorage: Boolean,
        val supportsMediaStore: Boolean,
        val supportsStorageStatsManager: Boolean,
        val supportsUsageStats: Boolean,
        val supportsTrash: Boolean,
        val supportsPartialMediaAccess: Boolean,
        val issues: List<String>
    ) {
        val isCompatible: Boolean
            get() = issues.isEmpty()
    }

    fun generateCompatibilityReport(context: Context): CompatibilityReport {
        val issues = mutableListOf<String>()

        val apiLevel = Build.VERSION.SDK_INT
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL

        // Scoped storage support (Android 10+)
        val supportsScopedStorage = apiLevel >= Build.VERSION_CODES.Q
        if (!supportsScopedStorage) {
            issues.add("Scoped storage not supported (API < 29)")
        }

        // MediaStore support (Android 10+ for MediaStore.VOLUME_EXTERNAL)
        val supportsMediaStore = apiLevel >= Build.VERSION_CODES.Q

        // StorageStatsManager (Android 8+)
        val supportsStorageStatsManager = apiLevel >= Build.VERSION_CODES.O

        // UsageStatsManager (Android 5+)
        val supportsUsageStats = apiLevel >= Build.VERSION_CODES.LOLLIPOP

        // Trash API (Android 11+)
        val supportsTrash = apiLevel >= Build.VERSION_CODES.R

        // Partial media access (Android 13+)
        val supportsPartialMediaAccess = apiLevel >= Build.VERSION_CODES.TIRAMISU

        // Check for known manufacturer issues
        checkManufacturerIssues(manufacturer, apiLevel, issues)

        // Check storage access
        checkStorageAccess(context, apiLevel, issues)

        // Check MediaStore columns
        checkMediaStoreCompatibility(apiLevel, issues)

        return CompatibilityReport(
            apiLevel = apiLevel,
            manufacturer = manufacturer,
            model = model,
            supportsScopedStorage = supportsScopedStorage,
            supportsMediaStore = supportsMediaStore,
            supportsStorageStatsManager = supportsStorageStatsManager,
            supportsUsageStats = supportsUsageStats,
            supportsTrash = supportsTrash,
            supportsPartialMediaAccess = supportsPartialMediaAccess,
            issues = issues
        )
    }

    private fun checkManufacturerIssues(manufacturer: String, apiLevel: Int, issues: MutableList<String>) {
        val lowerManufacturer = manufacturer.lowercase()
        
        when {
            lowerManufacturer.contains("samsung") && apiLevel < 30 -> {
                issues.add("Samsung devices on Android < 11 may have MediaStore indexing delays")
            }
            lowerManufacturer.contains("xiaomi") || lowerManufacturer.contains("redmi") || lowerManufacturer.contains("poco") -> {
                issues.add("Xiaomi devices may require manual permission for UsageStats")
            }
            lowerManufacturer.contains("huawei") || lowerManufacturer.contains("honor") -> {
                issues.add("Huawei/Honor devices may have restricted MediaStore access")
            }
            lowerManufacturer.contains("oneplus") && apiLevel < 29 -> {
                issues.add("OnePlus devices on Android < 10 may have storage permission issues")
            }
        }
    }

    private fun checkStorageAccess(context: Context, apiLevel: Int, issues: MutableList<String>) {
        // Check if we can access external storage
        val state = Environment.getExternalStorageState()
        if (state != Environment.MEDIA_MOUNTED) {
            issues.add("External storage not mounted: $state")
        }

        // Check if MediaStore is accessible
        try {
            val uri = if (apiLevel >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns._ID), null, null, null)?.use {
                // MediaStore accessible
            } ?: issues.add("MediaStore query returned null cursor")
        } catch (e: Exception) {
            issues.add("MediaStore access failed: ${e.message}")
        }
    }

    private fun checkMediaStoreCompatibility(apiLevel: Int, issues: MutableList<String>) {
        if (apiLevel >= Build.VERSION_CODES.Q) {
            // Check for RELATIVE_PATH support
            if (apiLevel < Build.VERSION_CODES.R) {
                issues.add("RELATIVE_PATH column requires Android 11+")
            }
        }
    }

    fun getRecommendedSettings(report: CompatibilityReport): List<String> {
        val recommendations = mutableListOf<String>()

        if (!report.supportsScopedStorage) {
            recommendations.add("Use legacy storage APIs with READ_EXTERNAL_STORAGE")
        }

        if (!report.supportsTrash) {
            recommendations.add("Deletion will be permanent (no trash/recycle bin)")
        }

        if (!report.supportsPartialMediaAccess) {
            recommendations.add("Cannot request partial media access; all-or-nothing permission")
        }

        if (report.manufacturer.lowercase().contains("xiaomi")) {
            recommendations.add("Guide user to enable 'Display pop-up windows' and 'Auto-start' for background scans")
        }

        if (report.manufacturer.lowercase().contains("samsung")) {
            recommendations.add("Use MediaStore.VOLUME_EXTERNAL_PRIMARY for better performance")
        }

        return recommendations
    }
}
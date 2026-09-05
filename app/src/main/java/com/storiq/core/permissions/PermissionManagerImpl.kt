package com.storiq.core.permissions

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope

class PermissionManagerImpl(private val context: Context) : PermissionManager {

    override suspend fun checkPermission(type: PermissionType): PermissionState {
        return when (type) {
            PermissionType.MEDIA_IMAGES -> checkMediaPermission("images")
            PermissionType.MEDIA_VIDEO -> checkMediaPermission("videos")
            PermissionType.MEDIA_AUDIO -> checkMediaPermission("audio")
            PermissionType.MANAGE_EXTERNAL_STORAGE -> checkManageExternalStorage()
            PermissionType.USAGE_STATS -> checkUsageStats()
            PermissionType.PACKAGE_USAGE_STATS -> checkPackageUsageStats()
            PermissionType.POST_NOTIFICATIONS -> checkPostNotifications()
        }
    }

    private fun checkMediaPermission(mediaType: String): PermissionState {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = when (mediaType) {
                "images" -> android.Manifest.permission.READ_MEDIA_IMAGES
                "videos" -> android.Manifest.permission.READ_MEDIA_VIDEO
                "audio" -> android.Manifest.permission.READ_MEDIA_AUDIO
                else -> android.Manifest.permission.READ_MEDIA_IMAGES
            }
            val granted = context.checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val shouldShowRationale = !granted && androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                context as FragmentActivity, permission
            )
            return PermissionState(
                type = type,
                status = if (granted) PermissionStatus.GRANTED else if (shouldShowRationale) PermissionStatus.DENIED else PermissionStatus.PERMANENTLY_DENIED,
                shouldShowRationale = shouldShowRationale
            )
        } else {
            val granted = context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val shouldShowRationale = !granted && androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                context as FragmentActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            return PermissionState(
                type = type,
                status = if (granted) PermissionStatus.GRANTED else if (shouldShowRationale) PermissionStatus.DENIED else PermissionStatus.PERMANENTLY_DENIED,
                shouldShowRationale = shouldShowRationale
            )
        }
    }

    private fun checkManageExternalStorage(): PermissionState {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val granted = Environment.isExternalStorageManager()
            return PermissionState(
                type = PermissionType.MANAGE_EXTERNAL_STORAGE,
                status = if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED,
                shouldShowRationale = !granted
            )
        } else {
            return PermissionState(
                type = PermissionType.MANAGE_EXTERNAL_STORAGE,
                status = PermissionStatus.GRANTED,
                shouldShowRationale = false
            )
        }
    }

    private fun checkUsageStats(): PermissionState {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        val granted = mode == AppOpsManager.MODE_ALLOWED
        return PermissionState(
            type = PermissionType.USAGE_STATS,
            status = if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED,
            shouldShowRationale = !granted
        )
    }

    private fun checkPackageUsageStats(): PermissionState {
        return checkUsageStats()
    }

    private fun checkPostNotifications(): PermissionState {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val shouldShowRationale = !granted && androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                context as FragmentActivity, android.Manifest.permission.POST_NOTIFICATIONS
            )
            return PermissionState(
                type = PermissionType.POST_NOTIFICATIONS,
                status = if (granted) PermissionStatus.GRANTED else if (shouldShowRationale) PermissionStatus.DENIED else PermissionStatus.PERMANENTLY_DENIED,
                shouldShowRationale = shouldShowRationale
            )
        } else {
            return PermissionState(
                type = PermissionType.POST_NOTIFICATIONS,
                status = PermissionStatus.GRANTED,
                shouldShowRationale = false
            )
        }
    }

    override suspend fun requestPermission(activity: FragmentActivity, type: PermissionType): PermissionRequestResult {
        return requestPermissions(activity, listOf(type))
    }

    override suspend fun requestPermissions(activity: FragmentActivity, types: List<PermissionType>): PermissionRequestResult {
        return coroutineScope {
            val granted = mutableListOf<PermissionType>()
            val denied = mutableListOf<PermissionType>()
            val permanentlyDenied = mutableListOf<PermissionType>()

            for (type in types) {
                val result = when (type) {
                    PermissionType.MEDIA_IMAGES -> requestMediaPermission(activity, "images")
                    PermissionType.MEDIA_VIDEO -> requestMediaPermission(activity, "videos")
                    PermissionType.MEDIA_AUDIO -> requestMediaPermission(activity, "audio")
                    PermissionType.MANAGE_EXTERNAL_STORAGE -> requestManageExternalStorage(activity)
                    PermissionType.USAGE_STATS -> requestUsageStats(activity)
                    PermissionType.PACKAGE_USAGE_STATS -> requestUsageStats(activity)
                    PermissionType.POST_NOTIFICATIONS -> requestPostNotifications(activity)
                }
                when (result) {
                    PermissionStatus.GRANTED -> granted.add(type)
                    PermissionStatus.DENIED -> denied.add(type)
                    PermissionStatus.PERMANENTLY_DENIED -> permanentlyDenied.add(type)
                    PermissionStatus.NOT_REQUESTED -> denied.add(type)
                }
            }

            PermissionRequestResult(granted, denied, permanentlyDenied)
        }
    }

    private suspend fun requestMediaPermission(activity: FragmentActivity, mediaType: String): PermissionStatus {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = when (mediaType) {
                "images" -> android.Manifest.permission.READ_MEDIA_IMAGES
                "videos" -> android.Manifest.permission.READ_MEDIA_VIDEO
                "audio" -> android.Manifest.permission.READ_MEDIA_AUDIO
                else -> android.Manifest.permission.READ_MEDIA_IMAGES
            }
            val launcher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                // Handled by coroutine
            }
            launcher.launch(permission)
            // Wait for result - simplified for now
            return checkMediaPermission(mediaType).status
        } else {
            val launcher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            }
            launcher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            return checkMediaPermission(mediaType).status
        }
    }

    private suspend fun requestManageExternalStorage(activity: FragmentActivity): PermissionStatus {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:${context.packageName}")
            activity.startActivity(intent)
            return checkManageExternalStorage().status
        }
        return PermissionStatus.GRANTED
    }

    private suspend fun requestUsageStats(activity: FragmentActivity): PermissionStatus {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        activity.startActivity(intent)
        return checkUsageStats().status
    }

    private suspend fun requestPostNotifications(activity: FragmentActivity): PermissionStatus {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val launcher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            }
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            return checkPostNotifications().status
        }
        return PermissionStatus.GRANTED
    }

    override fun getPermissionRationale(type: PermissionType): String {
        return when (type) {
            PermissionType.MEDIA_IMAGES -> "StorIQ needs access to your photos to analyze storage usage, find duplicates, and identify large images."
            PermissionType.MEDIA_VIDEO -> "StorIQ needs access to your videos to analyze storage usage, find duplicates, and identify large videos."
            PermissionType.MEDIA_AUDIO -> "StorIQ needs access to your audio files to analyze storage usage and identify large audio files."
            PermissionType.MANAGE_EXTERNAL_STORAGE -> "StorIQ needs full storage access to scan all files, including downloads and documents, for comprehensive storage analysis."
            PermissionType.USAGE_STATS -> "StorIQ needs usage stats access to detect unused apps and show when apps were last used."
            PermissionType.PACKAGE_USAGE_STATS -> "StorIQ needs usage stats access to detect unused apps and show when apps were last used."
            PermissionType.POST_NOTIFICATIONS -> "StorIQ can send notifications when background scans complete or when storage is running low."
        }
    }

    override fun openAppSettings(type: PermissionType) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:${context.packageName}")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}
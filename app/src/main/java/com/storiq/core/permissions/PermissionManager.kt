package com.storiq.core.permissions

import android.content.Context
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity

interface PermissionManager {
    enum class PermissionType {
        MEDIA_IMAGES,
        MEDIA_VIDEO,
        MEDIA_AUDIO,
        MANAGE_EXTERNAL_STORAGE,
        USAGE_STATS,
        PACKAGE_USAGE_STATS,
        POST_NOTIFICATIONS
    }

    enum class PermissionStatus {
        GRANTED,
        DENIED,
        PERMANENTLY_DENIED,
        NOT_REQUESTED
    }

    data class PermissionState(
        val type: PermissionType,
        val status: PermissionStatus,
        val shouldShowRationale: Boolean
    )

    data class PermissionRequestResult(
        val granted: List<PermissionType>,
        val denied: List<PermissionType>,
        val permanentlyDenied: List<PermissionType>
    )

    suspend fun checkPermission(type: PermissionType): PermissionState
    suspend fun requestPermission(activity: FragmentActivity, type: PermissionType): PermissionRequestResult
    suspend fun requestPermissions(activity: FragmentActivity, types: List<PermissionType>): PermissionRequestResult
    fun getPermissionRationale(type: PermissionType): String
    fun openAppSettings(type: PermissionType)
}
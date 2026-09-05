package com.storiq.core.media

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageStatsManager
import com.storiq.core.model.AppRecord
import com.storiq.core.storage.StorageRepository
import com.storiq.core.util.CoroutineDispatcherProvider
import kotlinx.coroutines.withContext

class AppScanner(
    private val context: Context,
    private val repository: StorageRepository,
    private val dispatcherProvider: CoroutineDispatcherProvider
) {

    suspend fun scanApps(scanSessionId: Long): ScanResult {
        return withContext(dispatcherProvider.io) {
            val packageManager = context.packageManager
            val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            var count = 0
            var totalSize = 0L
            val errors = mutableListOf<String>()
            val records = mutableListOf<AppRecord>()

            val storageStatsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.getSystemService(StorageStatsManager::class.java)
            } else null

            val storageManager = context.getSystemService(StorageManager::class.java)

            for (appInfo in apps) {
                try {
                    val packageName = appInfo.packageName
                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                    // Get app storage info
                    val (appSize, userDataSize, cacheSize) = getAppStorageSize(packageName, storageStatsManager, storageManager)
                    val totalSize = appSize + userDataSize + cacheSize

                    val record = AppRecord(
                        packageName = packageName,
                        appName = appName,
                        totalBytes = totalSize,
                        appBytes = appSize,
                        userDataBytes = userDataSize,
                        cacheBytes = cacheSize,
                        installDate = getInstallTime(packageManager, packageName),
                        lastUsedTimestamp = getLastUsedTime(packageManager, packageName),
                        isSystemApp = isSystemApp,
                        versionName = getVersionName(packageManager, packageName),
                        versionCode = getVersionCode(packageManager, packageName)
                    )

                    records.add(record)
                    count++
                    totalSize += totalSize
                } catch (e: Exception) {
                    errors.add("Failed to scan ${appInfo.packageName}: ${e.message}")
                }
            }

            if (records.isNotEmpty()) {
                repository.database.appRecordDao().insertAll(records)
            }

            ScanResult(
                success = true,
                totalScanned = count,
                totalSize = totalSize,
                errors = errors
            )
        }
    }

    private fun getAppStorageSize(
        packageName: String,
        storageStatsManager: StorageStatsManager?,
        storageManager: StorageManager?
    ): Triple<Long, Long, Long> {
        var appSize = 0L
        var userDataSize = 0L
        var cacheSize = 0L

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && storageStatsManager != null) {
            try {
                val stats = storageStatsManager.getStatsForPackage(
                    Environment.getDataDirectory().absolutePath,
                    packageName,
                    android.os.UserHandle.myUserId()
                )
                appSize = stats.appBytes
                userDataSize = stats.dataBytes
                cacheSize = stats.cacheBytes
            } catch (e: Exception) {
                // Fallback to manual calculation
                val appInfo = try {
                    context.packageManager.getApplicationInfo(packageName, 0)
                } catch (e: Exception) {
                    return Triple(0L, 0L, 0L)
                }
                appSize = getDirSize(java.io.File(appInfo.sourceDir))
                userDataSize = getDirSize(java.io.File(appInfo.dataDir))
                cacheSize = getDirSize(context.getCacheDir())
            }
        } else {
            // Pre-Oreo fallback
            val appInfo = try {
                context.packageManager.getApplicationInfo(packageName, 0)
            } catch (e: Exception) {
                return Triple(0L, 0L, 0L)
            }
            appSize = getDirSize(java.io.File(appInfo.sourceDir))
            userDataSize = getDirSize(java.io.File(appInfo.dataDir))
            cacheSize = getDirSize(context.getCacheDir())
        }

        return Triple(appSize, userDataSize, cacheSize)
    }

    private fun getDirSize(file: java.io.File): Long {
        if (!file.exists()) return 0L
        if (file.isFile) return file.length()
        var size = 0L
        file.listFiles()?.forEach { size += getDirSize(it) }
        return size
    }

    private fun getInstallTime(packageManager: PackageManager, packageName: String): Long {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                packageManager.getPackageInfo(packageName, 0).firstInstallTime
            } else {
                System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun getLastUsedTime(packageManager: PackageManager, packageName: String): Long? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val info = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
                if (info.lastUpdateTime > 0) info.lastUpdateTime.toLong() else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun getVersionName(packageManager: PackageManager, packageName: String): String? {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            null
        }
    }

    private fun getVersionCode(packageManager: PackageManager, packageName: String): Int? {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionCode
        } catch (e: Exception) {
            null
        }
    }

    data class ScanResult(
        val success: Boolean,
        val totalScanned: Int = 0,
        val totalSize: Long = 0L,
        val errors: List<String> = emptyList(),
        val errorMessage: String? = null
    )
}
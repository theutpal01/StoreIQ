package com.storiq.core.media

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.storiq.AppEntryPoint
import com.storiq.core.storage.StorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScanWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val storageRepository: StorageRepository by lazy {
        AppEntryPoint.get(applicationContext).storageRepository()
    }

    override suspend fun doWork(): Result {
        return try {
            val result = withContext(Dispatchers.IO) {
                storageRepository.performFullScan()
            }
            
            if (result.success) {
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "full_storage_scan"
        const val UNIQUE_WORK_NAME = "unique_full_storage_scan"
        
        fun schedulePeriodicScan(context: Context) {
            val workRequest = androidx.work.PeriodicWorkRequestBuilder<ScanWorker>(
                24 * 60 * 60 * 1000L, // 24 hours
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
                .setInitialDelay(1, java.util.concurrent.TimeUnit.HOURS)
                .addTag("storage_scan")
                .build()
            
            androidx.work.WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    UNIQUE_WORK_NAME,
                    androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
        }
        
        fun scheduleOneTimeScan(context: Context) {
            val workRequest = androidx.work.OneTimeWorkRequestBuilder<ScanWorker>()
                .setInitialDelay(5, java.util.concurrent.TimeUnit.SECONDS)
                .addTag("storage_scan")
                .build()
            
            androidx.work.WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_WORK_NAME,
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }
        
        fun cancelAllScans(context: Context) {
            androidx.work.WorkManager.getInstance(context)
                .cancelAllWorkByTag("storage_scan")
        }
    }
}
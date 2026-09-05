package com.storiq.core.storage

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import androidx.datastore.preferences.PreferencesDataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.lifecycle.lifecycleScope
import com.storiq.core.database.StorIQDatabase
import com.storiq.core.model.*
import com.storiq.core.util.CoroutineDispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class StorageRepositoryImpl(
    private val database: StorIQDatabase,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val context: Context,
    private val preferencesDataStore: PreferencesDataStore
) : StorageRepository {

    override val database: StorIQDatabase
        get() = database

    private val _storageBreakdown = MutableStateFlow<List<StorageBreakdown>>(emptyList())
    override val observeStorageBreakdown: kotlinx.coroutines.flow.Flow<List<StorageBreakdown>> = _storageBreakdown

    private val _storageHistory = MutableStateFlow<List<StorageSnapshot>>(emptyList())
    override val observeStorageHistory: kotlinx.coroutines.flow.Flow<List<StorageSnapshot>> = _storageHistory

    private val isScanning = AtomicBoolean(false)
    private val mediaScanner by lazy { MediaScanner(context, this, dispatcherProvider) }
    private val fileScanner by lazy { FileScanner(context, this, dispatcherProvider) }
    private val appScanner by lazy { AppScanner(context, this, dispatcherProvider) }
    private val deletionManager by lazy { DeletionManager(context) }

    override suspend fun getTotalStorageBytes(): Long {
        return withContext(dispatcherProvider.io) {
            val file = Environment.getDataDirectory()
            file.totalSpace
        }
    }

    override suspend fun getUsedStorageBytes(): Long {
        return withContext(dispatcherProvider.io) {
            val file = Environment.getDataDirectory()
            file.totalSpace - file.freeSpace
        }
    }

    override suspend fun getFreeStorageBytes(): Long {
        return withContext(dispatcherProvider.io) {
            val file = Environment.getDataDirectory()
            file.freeSpace
        }
    }

    override suspend fun getStorageUsagePercent(): Float {
        return withContext(dispatcherProvider.io) {
            val total = getTotalStorageBytes()
            val used = getUsedStorageBytes()
            if (total > 0) (used.toFloat() / total) * 100 else 0f
        }
    }

    override suspend fun getStorageBreakdown(): List<StorageBreakdown> {
        return withContext(dispatcherProvider.io) {
            val total = getTotalStorageBytes()
            val breakdown = mutableListOf<StorageBreakdown>()

            val appSize = getTotalAppSize()
            val mediaSize = getTotalMediaSize()
            val docsSize = getTotalSizeByCategory(MediaCategory.DOCUMENTS) +
                    getTotalSizeByCategory(MediaCategory.DOWNLOADS) +
                    getTotalSizeByCategory(MediaCategory.ARCHIVES)
            val otherSize = total - appSize - mediaSize - docsSize

            if (appSize > 0) breakdown.add(StorageBreakdown(MediaCategory.APPS, appSize, 0))
            if (mediaSize > 0) breakdown.add(StorageBreakdown(MediaCategory.VIDEOS, mediaSize, 0))
            if (docsSize > 0) breakdown.add(StorageBreakdown(MediaCategory.DOCUMENTS, docsSize, 0))
            if (otherSize > 0) breakdown.add(StorageBreakdown(MediaCategory.OTHER, otherSize.coerceAtLeast(0), 0))

            _storageBreakdown.value = breakdown
            breakdown
        }
    }

    private suspend fun getTotalSizeByCategory(category: MediaCategory): Long {
        return withContext(dispatcherProvider.io) {
            database.mediaRecordDao().getTotalSizeByCategory(category) ?: 0L
        }
    }

    override suspend fun scanMedia(): ScanSession {
        return withContext(dispatcherProvider.io) {
            if (isScanning.getAndSet(true)) {
                return@withContext ScanSession(status = ScanStatus.IN_PROGRESS, errorMessage = "Scan already in progress")
            }

            val session = ScanSession(
                scanType = ScanType.MEDIA_ONLY,
                status = ScanStatus.IN_PROGRESS
            )
            val sessionId = database.scanSessionDao().insert(session)

            try {
                val result = mediaScanner.scanMedia(sessionId)
                
                val updatedSession = session.copy(
                    id = sessionId,
                    status = if (result.success) ScanStatus.COMPLETED else ScanStatus.FAILED,
                    completedAt = System.currentTimeMillis(),
                    totalMediaScanned = result.totalScanned,
                    totalBytesScanned = result.totalSize,
                    errorMessage = if (result.success) null else result.errors.joinToString("; ")
                )
                database.scanSessionDao().update(updatedSession)
                
                // Compute hashes for duplicate detection
                if (result.success) {
                    mediaScanner.computeHashesForDuplicates(sessionId)
                }
                
                updatedSession
            } catch (e: Exception) {
                val failedSession = session.copy(
                    id = sessionId,
                    status = ScanStatus.FAILED,
                    completedAt = System.currentTimeMillis(),
                    errorMessage = e.message
                )
                database.scanSessionDao().update(failedSession)
                failedSession
            } finally {
                isScanning.set(false)
            }
        }
    }

    override suspend fun getMediaByCategory(category: MediaCategory): List<MediaRecord> {
        return withContext(dispatcherProvider.io) {
            database.mediaRecordDao().getByCategory(category)
        }
    }

    override suspend fun getLargeMedia(minSize: Long): List<MediaRecord> {
        return withContext(dispatcherProvider.io) {
            database.mediaRecordDao().getLargeFiles(minSize)
        }
    }

    override suspend fun getScreenshots(): List<MediaRecord> {
        return withContext(dispatcherProvider.io) {
            database.mediaRecordDao().getScreenshots()
        }
    }

    override suspend fun getScreenRecordings(): List<MediaRecord> {
        return withContext(dispatcherProvider.io) {
            database.mediaRecordDao().getScreenRecordings()
        }
    }

    override suspend fun getAllMediaWithHash(): List<MediaRecord> {
        return withContext(dispatcherProvider.io) {
            database.mediaRecordDao().getAllWithHash()
        }
    }

    override suspend fun getMediaCount(): Int {
        return withContext(dispatcherProvider.io) {
            database.mediaRecordDao().getTotalCount()
        }
    }

    override suspend fun getTotalMediaSize(): Long {
        return withContext(dispatcherProvider.io) {
            database.mediaRecordDao().getTotalSize() ?: 0L
        }
    }

    override suspend fun scanFiles(): ScanSession {
        return withContext(dispatcherProvider.io) {
            if (isScanning.getAndSet(true)) {
                return@withContext ScanSession(status = ScanStatus.IN_PROGRESS, errorMessage = "Scan already in progress")
            }

            val session = ScanSession(
                scanType = ScanType.FULL,
                status = ScanStatus.IN_PROGRESS
            )
            val sessionId = database.scanSessionDao().insert(session)

            try {
                val result = fileScanner.scanDownloads(sessionId)
                
                val updatedSession = session.copy(
                    id = sessionId,
                    status = if (result.success) ScanStatus.COMPLETED else ScanStatus.FAILED,
                    completedAt = System.currentTimeMillis(),
                    totalFilesScanned = result.totalScanned,
                    totalBytesScanned = result.totalSize,
                    errorMessage = if (result.success) null else result.errors.joinToString("; ")
                )
                database.scanSessionDao().update(updatedSession)
                
                if (result.success) {
                    fileScanner.computeHashesForDuplicates()
                }
                
                updatedSession
            } catch (e: Exception) {
                val failedSession = session.copy(
                    id = sessionId,
                    status = ScanStatus.FAILED,
                    completedAt = System.currentTimeMillis(),
                    errorMessage = e.message
                )
                database.scanSessionDao().update(failedSession)
                failedSession
            } finally {
                isScanning.set(false)
            }
        }
    }

    override suspend fun getFilesByCategory(category: MediaCategory): List<FileRecord> {
        return withContext(dispatcherProvider.io) {
            database.fileRecordDao().getByCategory(category)
        }
    }

    override suspend fun getLargeFiles(minSize: Long): List<FileRecord> {
        return withContext(dispatcherProvider.io) {
            database.fileRecordDao().getLargeFiles(minSize)
        }
    }

    override suspend fun getAllFilesWithHash(): List<FileRecord> {
        return withContext(dispatcherProvider.io) {
            database.fileRecordDao().getAllWithHash()
        }
    }

    override suspend fun scanApps(): ScanSession {
        return withContext(dispatcherProvider.io) {
            if (isScanning.getAndSet(true)) {
                return@withContext ScanSession(status = ScanStatus.IN_PROGRESS, errorMessage = "Scan already in progress")
            }

            val session = ScanSession(
                scanType = ScanType.APPS_ONLY,
                status = ScanStatus.IN_PROGRESS
            )
            val sessionId = database.scanSessionDao().insert(session)

            try {
                val result = appScanner.scanApps(sessionId)
                
                val updatedSession = session.copy(
                    id = sessionId,
                    status = if (result.success) ScanStatus.COMPLETED else ScanStatus.FAILED,
                    completedAt = System.currentTimeMillis(),
                    totalAppsScanned = result.totalScanned,
                    totalBytesScanned = result.totalSize,
                    errorMessage = if (result.success) null else result.errors.joinToString("; ")
                )
                database.scanSessionDao().update(updatedSession)
                updatedSession
            } catch (e: Exception) {
                val failedSession = session.copy(
                    id = sessionId,
                    status = ScanStatus.FAILED,
                    completedAt = System.currentTimeMillis(),
                    errorMessage = e.message
                )
                database.scanSessionDao().update(failedSession)
                failedSession
            } finally {
                isScanning.set(false)
            }
        }
    }

    override suspend fun getAllApps(): List<AppRecord> {
        return withContext(dispatcherProvider.io) {
            database.appRecordDao().getAllSortedBySize()
        }
    }

    override suspend fun getUserApps(): List<AppRecord> {
        return withContext(dispatcherProvider.io) {
            database.appRecordDao().getUserAppsSortedBySize()
        }
    }

    override suspend fun getUnusedApps(daysThreshold: Int): List<AppRecord> {
        return withContext(dispatcherProvider.io) {
            val cutoffTime = System.currentTimeMillis() - (daysThreshold * 24L * 60 * 60 * 1000)
            database.appRecordDao().getUnusedApps(cutoffTime)
        }
    }

    override suspend fun getLargeApps(minSize: Long): List<AppRecord> {
        return withContext(dispatcherProvider.io) {
            database.appRecordDao().getLargeApps(minSize)
        }
    }

    override suspend fun getTotalAppSize(): Long {
        return withContext(dispatcherProvider.io) {
            database.appRecordDao().getTotalAppSize() ?: 0L
        }
    }

    override suspend fun getTotalUserAppSize(): Long {
        return withContext(dispatcherProvider.io) {
            database.appRecordDao().getTotalUserAppSize() ?: 0L
        }
    }

    override suspend fun findDuplicates(): List<DuplicateGroup> {
        return withContext(dispatcherProvider.io) {
            val mediaWithHash = database.mediaRecordDao().getAllWithHash()
            val filesWithHash = database.fileRecordDao().getAllWithHash()
            
            val allRecords = mutableListOf<Any>()
            allRecords.addAll(mediaWithHash)
            allRecords.addAll(filesWithHash)
            
            val groups = mutableMapOf<String, MutableList<Any>>()

            for (record in allRecords) {
                val hash = when (record) {
                    is MediaRecord -> record.hash
                    is FileRecord -> record.hash
                    else -> null
                }
                hash?.let { h ->
                    groups.getOrPut(h) { mutableListOf() }.add(record)
                }
            }

            val duplicateGroups = mutableListOf<DuplicateGroup>()
            for ((hash, records) in groups) {
                if (records.size > 1) {
                    val first = records.first()
                    val (sizeBytes, category) = when (first) {
                        is MediaRecord -> first.sizeBytes to first.category
                        is FileRecord -> first.sizeBytes to first.category
                        else -> 0L to MediaCategory.OTHER
                    }
                    
                    val recommendedKeep = records.maxByOrNull { 
                        when (it) {
                            is MediaRecord -> it.modifiedDate
                            is FileRecord -> it.modifiedDate
                            else -> 0L
                        }
                    }
                    
                    val recommendedKeepUri = when (recommendedKeep) {
                        is MediaRecord -> recommendedKeep.uri
                        is FileRecord -> recommendedKeep.uri
                        else -> null
                    }

                    val group = DuplicateGroup(
                        hash = hash,
                        sizeBytes = sizeBytes,
                        count = records.size,
                        category = category,
                        recommendedKeepUri = recommendedKeepUri,
                        scanSessionId = 0
                    )
                    duplicateGroups.add(group)
                }
            }

            database.duplicateGroupDao().insertAll(duplicateGroups)
            duplicateGroups
        }
    }

    override suspend fun getDuplicateGroups(): List<DuplicateGroup> {
        return withContext(dispatcherProvider.io) {
            database.duplicateGroupDao().getAllSortedByWastedSpace()
        }
    }

    override suspend fun getTotalWastedSpace(): Long {
        return withContext(dispatcherProvider.io) {
            database.duplicateGroupDao().getTotalWastedSpace() ?: 0L
        }
    }

    override suspend fun generateCleanupRecommendations(): List<CleanupItem> {
        return withContext(dispatcherProvider.io) {
            val items = mutableListOf<CleanupItem>()

            // Duplicates
            val duplicates = getDuplicateGroups()
            if (duplicates.isNotEmpty()) {
                val totalWasted = duplicates.sumOf { it.totalWastedBytes }
                items.add(CleanupItem(
                    category = CleanupCategory.DUPLICATES,
                    title = "Duplicate Files",
                    description = "${duplicates.size} duplicate groups found",
                    whyRecommended = "Exact duplicate files are wasting space. Keep one copy and remove the rest.",
                    whatHappensIfDeleted = "Duplicate copies will be moved to trash. Original files remain.",
                    estimatedBytes = totalWasted,
                    risk = CleanupRisk.LOW,
                    relatedUris = duplicates.flatMap { it.recommendedKeepUri?.let { listOf(it) } ?: emptyList() }
                ))
            }

            // Old downloads
            // TODO: Add actual download scanning
            // For now, placeholder

            database.cleanupItemDao().insertAll(items)
            items
        }
    }

    override suspend fun getCleanupItems(): List<CleanupItem> {
        return withContext(dispatcherProvider.io) {
            database.cleanupItemDao().getAllSortedBySize()
        }
    }

    override suspend fun updateCleanupItemSelection(itemId: Long, isSelected: Boolean) {
        withContext(dispatcherProvider.io) {
            // TODO: Implement selection update
        }
    }

    override suspend fun getSelectedCleanupItems(): List<CleanupItem> {
        return withContext(dispatcherProvider.io) {
            database.cleanupItemDao().getSelected()
        }
    }

    override suspend fun getSelectedCleanupTotal(): Pair<Int, Long> {
        return withContext(dispatcherProvider.io) {
            val count = database.cleanupItemDao().getSelectedCount()
            val bytes = database.cleanupItemDao().getSelectedTotalBytes() ?: 0L
            Pair(count, bytes)
        }
    }

    override suspend fun generateRecommendations(): List<Recommendation> {
        return withContext(dispatcherProvider.io) {
            val recommendations = mutableListOf<Recommendation>()
            val usagePercent = getStorageUsagePercent()

            if (usagePercent > 90f) {
                recommendations.add(Recommendation(
                    type = RecommendationType.HIGH_STORAGE_USAGE,
                    priority = RecommendationPriority.URGENT,
                    title = "Storage Almost Full",
                    description = "Your storage is ${String.format("%.0f", usagePercent)}% full. Consider cleaning up space.",
                    estimatedRecoverableBytes = 0,
                    actionLabel = "Review Cleanup",
                    actionRoute = "clean"
                ))
            }

            val duplicateWasted = getTotalWastedSpace()
            if (duplicateWasted > 100 * 1024 * 1024) { // 100MB
                recommendations.add(Recommendation(
                    type = RecommendationType.DUPLICATES_FOUND,
                    priority = RecommendationPriority.HIGH,
                    title = "Duplicate Files Found",
                    description = "${StorageBreakdown.formatBytes(duplicateWasted)} in duplicate files",
                    estimatedRecoverableBytes = duplicateWasted,
                    actionLabel = "Review Duplicates",
                    actionRoute = "duplicates"
                ))
            }

            database.recommendationDao().insertAll(recommendations)
            recommendations
        }
    }

    override suspend fun getActiveRecommendations(): List<Recommendation> {
        return withContext(dispatcherProvider.io) {
            database.recommendationDao().getActive()
        }
    }

    override suspend fun dismissRecommendation(id: Long) {
        withContext(dispatcherProvider.io) {
            database.recommendationDao().dismiss(id)
        }
    }

    override suspend fun saveStorageSnapshot(snapshot: StorageSnapshot) {
        withContext(dispatcherProvider.io) {
            database.storageSnapshotDao().insert(snapshot)
        }
    }

    override suspend fun getLatestSnapshot(): StorageSnapshot? {
        return withContext(dispatcherProvider.io) {
            database.storageSnapshotDao().getLatest()
        }
    }

    override suspend fun getStorageHistory(limit: Int): List<StorageSnapshot> {
        return withContext(dispatcherProvider.io) {
            database.storageSnapshotDao().getRecent(limit)
        }
    }

    override suspend fun getStorageHistoryRange(startTime: Long, endTime: Long): List<StorageSnapshot> {
        return withContext(dispatcherProvider.io) {
            database.storageSnapshotDao().getRange(startTime, endTime)
        }
    }

    override suspend fun createSwipeSession(mediaType: SwipeMediaType): SwipeSession {
        return withContext(dispatcherProvider.io) {
            val session = SwipeSession(
                mediaType = mediaType,
                totalItems = 0,
                keptItems = 0,
                deleteCandidates = 0,
                totalCandidateBytes = 0
            )
            session.copy(id = database.swipeSessionDao().insert(session))
        }
    }

    override suspend fun updateSwipeSession(session: SwipeSession) {
        withContext(dispatcherProvider.io) {
            database.swipeSessionDao().update(session)
        }
    }

    override suspend fun getLatestSwipeSession(): SwipeSession? {
        return withContext(dispatcherProvider.io) {
            database.swipeSessionDao().getLatest()
        }
    }

    override suspend fun getSwipeSessionById(id: Long): SwipeSession? {
        return withContext(dispatcherProvider.io) {
            database.swipeSessionDao().getById(id)
        }
    }

    override suspend fun saveSwipeDecisions(decisions: List<SwipeDecisionRecord>) {
        withContext(dispatcherProvider.io) {
            database.swipeDecisionRecordDao().insertAll(decisions)
        }
    }

    override suspend fun getSwipeDecisions(sessionId: Long): List<SwipeDecisionRecord> {
        return withContext(dispatcherProvider.io) {
            database.swipeDecisionRecordDao().getBySessionId(sessionId)
        }
    }

    override suspend fun getSwipeDecisionsByType(sessionId: Long, decision: SwipeDecision): List<SwipeDecisionRecord> {
        return withContext(dispatcherProvider.io) {
            database.swipeDecisionRecordDao().getBySessionAndDecision(sessionId, decision)
        }
    }

    override suspend fun deleteMediaItems(uris: List<String>): DeletionResult {
        return deletionManager.deleteMediaItems(uris)
    }

    override suspend fun deleteFileItems(uris: List<String>): DeletionResult {
        return deletionManager.deleteFileItems(uris)
    }

    override suspend fun createScanSession(type: ScanType): ScanSession {
        return withContext(dispatcherProvider.io) {
            val session = ScanSession(
                scanType = type,
                status = ScanStatus.PENDING
            )
            session.copy(id = database.scanSessionDao().insert(session))
        }
    }

    override suspend fun updateScanSession(session: ScanSession) {
        withContext(dispatcherProvider.io) {
            database.scanSessionDao().update(session)
        }
    }

    override suspend fun getLatestScanSession(): ScanSession? {
        return withContext(dispatcherProvider.io) {
            database.scanSessionDao().getLatest()
        }
    }

    override suspend fun computeMediaHashes(): Int {
        return withContext(dispatcherProvider.io) {
            mediaScanner.computeHashesForDuplicates(0)
        }
    }

    suspend fun performFullScan(): FullScanResult {
        return withContext(dispatcherProvider.io) {
            if (isScanning.getAndSet(true)) {
                return@withContext FullScanResult(success = false, errorMessage = "Scan already in progress")
            }

            val startTime = System.currentTimeMillis()
            val errors = mutableListOf<String>()
            var mediaScanned = 0
            var filesScanned = 0
            var appsScanned = 0

            try {
                // 1. Scan Media
                val mediaSession = createScanSession(ScanType.MEDIA_ONLY)
                val mediaResult = mediaScanner.scanMedia(mediaSession.id)
                mediaScanned = mediaResult.totalScanned
                errors.addAll(mediaResult.errors)
                
                if (mediaResult.success) {
                    mediaScanner.computeHashesForDuplicates(mediaSession.id)
                }

                // 2. Scan Files
                val fileSession = createScanSession(ScanType.FULL)
                val fileResult = fileScanner.scanDownloads(fileSession.id)
                filesScanned = fileResult.totalScanned
                errors.addAll(fileResult.errors)

                if (fileResult.success) {
                    fileScanner.computeHashesForDuplicates()
                }

                // 3. Scan Apps
                val appSession = createScanSession(ScanType.APPS_ONLY)
                val appResult = appScanner.scanApps(appSession.id)
                appsScanned = appResult.totalScanned
                errors.addAll(appResult.errors)

                // 4. Find Duplicates
                findDuplicates()

                // 5. Generate Cleanup Recommendations
                generateCleanupRecommendations()

                // 6. Generate Recommendations
                generateRecommendations()

                // 7. Save Storage Snapshot
                val snapshot = StorageSnapshot(
                    totalBytes = getTotalStorageBytes(),
                    usedBytes = getUsedStorageBytes(),
                    freeBytes = getFreeStorageBytes(),
                    appBytes = getTotalAppSize(),
                    mediaBytes = getTotalMediaSize(),
                    documentsBytes = getTotalSizeByCategory(MediaCategory.DOCUMENTS) +
                            getTotalSizeByCategory(MediaCategory.DOWNLOADS) +
                            getTotalSizeByCategory(MediaCategory.ARCHIVES),
                    otherBytes = getTotalStorageBytes() - getTotalAppSize() - getTotalMediaSize() - 
                            (getTotalSizeByCategory(MediaCategory.DOCUMENTS) +
                                    getTotalSizeByCategory(MediaCategory.DOWNLOADS) +
                                    getTotalSizeByCategory(MediaCategory.ARCHIVES)).coerceAtLeast(0)
                )
                saveStorageSnapshot(snapshot)

                FullScanResult(
                    success = true,
                    mediaScanned = mediaScanned,
                    filesScanned = filesScanned,
                    appsScanned = appsScanned,
                    durationMs = System.currentTimeMillis() - startTime,
                    errors = errors
                )
            } catch (e: Exception) {
                FullScanResult(success = false, errorMessage = e.message)
            } finally {
                isScanning.set(false)
            }
        }
    }

    data class FullScanResult(
        val success: Boolean,
        val mediaScanned: Int = 0,
        val filesScanned: Int = 0,
        val appsScanned: Int = 0,
        val durationMs: Long = 0,
        val errors: List<String> = emptyList(),
        val errorMessage: String? = null
    )
}
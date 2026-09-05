package com.storiq.core.storage

import com.storiq.core.model.*
import kotlinx.coroutines.flow.Flow

interface StorageRepository {
    // Storage Statistics
    suspend fun getTotalStorageBytes(): Long
    suspend fun getUsedStorageBytes(): Long
    suspend fun getFreeStorageBytes(): Long
    suspend fun getStorageUsagePercent(): Float

    // Storage Breakdown
    suspend fun getStorageBreakdown(): List<StorageBreakdown>
    suspend fun observeStorageBreakdown(): Flow<List<StorageBreakdown>>

    // Media Records
    suspend fun scanMedia(): ScanSession
    suspend fun getMediaByCategory(category: MediaCategory): List<MediaRecord>
    suspend fun getLargeMedia(minSize: Long): List<MediaRecord>
    suspend fun getScreenshots(): List<MediaRecord>
    suspend fun getScreenRecordings(): List<MediaRecord>
    suspend fun getAllMediaWithHash(): List<MediaRecord>
    suspend fun getMediaCount(): Int
    suspend fun getTotalMediaSize(): Long
    suspend fun computeMediaHashes(): Int

    // File Records
    suspend fun scanFiles(): ScanSession
    suspend fun getFilesByCategory(category: MediaCategory): List<FileRecord>
    suspend fun getLargeFiles(minSize: Long): List<FileRecord>
    suspend fun getAllFilesWithHash(): List<FileRecord>

    // App Records
    suspend fun scanApps(): ScanSession
    suspend fun getAllApps(): List<AppRecord>
    suspend fun getUserApps(): List<AppRecord>
    suspend fun getUnusedApps(daysThreshold: Int): List<AppRecord>
    suspend fun getLargeApps(minSize: Long): List<AppRecord>
    suspend fun getTotalAppSize(): Long
    suspend fun getTotalUserAppSize(): Long

    // Duplicate Detection
    suspend fun findDuplicates(): List<DuplicateGroup>
    suspend fun getDuplicateGroups(): List<DuplicateGroup>
    suspend fun getTotalWastedSpace(): Long

    // Cleanup Items
    suspend fun generateCleanupRecommendations(): List<CleanupItem>
    suspend fun getCleanupItems(): List<CleanupItem>
    suspend fun updateCleanupItemSelection(itemId: Long, isSelected: Boolean)
    suspend fun getSelectedCleanupItems(): List<CleanupItem>
    suspend fun getSelectedCleanupTotal(): Pair<Int, Long>

    // Recommendations
    suspend fun generateRecommendations(): List<Recommendation>
    suspend fun getActiveRecommendations(): List<Recommendation>
    suspend fun dismissRecommendation(id: Long)

    // Storage History
    suspend fun saveStorageSnapshot(snapshot: StorageSnapshot)
    suspend fun getLatestSnapshot(): StorageSnapshot?
    suspend fun getStorageHistory(limit: Int): List<StorageSnapshot>
    suspend fun getStorageHistoryRange(startTime: Long, endTime: Long): List<StorageSnapshot>
    suspend fun observeStorageHistory(): Flow<List<StorageSnapshot>>

    // Swipe Clean
    suspend fun createSwipeSession(mediaType: SwipeMediaType): SwipeSession
    suspend fun updateSwipeSession(session: SwipeSession)
    suspend fun getLatestSwipeSession(): SwipeSession?
    suspend fun getSwipeSessionById(id: Long): SwipeSession?
    suspend fun saveSwipeDecisions(decisions: List<SwipeDecisionRecord>)
    suspend fun getSwipeDecisions(sessionId: Long): List<SwipeDecisionRecord>
    suspend fun getSwipeDecisionsByType(sessionId: Long, decision: SwipeDecision): List<SwipeDecisionRecord>

    // Deletion
    suspend fun deleteMediaItems(uris: List<String>): DeletionResult
    suspend fun deleteFileItems(uris: List<String>): DeletionResult

    // Scan Sessions
    suspend fun createScanSession(type: ScanType): ScanSession
    suspend fun updateScanSession(session: ScanSession)
    suspend fun getLatestScanSession(): ScanSession?
    suspend fun performFullScan(): StorageRepositoryImpl.FullScanResult

    val database: StorIQDatabase
}

data class DeletionResult(
    val success: Boolean,
    val deletedCount: Int,
    val failedCount: Int,
    val deletedBytes: Long,
    val errors: List<String> = emptyList()
)
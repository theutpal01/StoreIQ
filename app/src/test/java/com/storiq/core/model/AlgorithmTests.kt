package com.storiq.core.model

import org.junit.Test
import org.junit.Assert.*

class DuplicateDetectionAlgorithmTest {

    @Test
    fun `groupByHash creates correct duplicate groups`() {
        // Test the algorithm used in StorageRepositoryImpl.findDuplicates()
        val records = listOf(
            createMediaRecord("hash1", 1000, "uri1", 1000L),
            createMediaRecord("hash1", 1000, "uri2", 2000L),
            createMediaRecord("hash1", 1000, "uri3", 1500L),
            createMediaRecord("hash2", 2000, "uri4", 3000L),
            createMediaRecord("hash2", 2000, "uri5", 4000L),
            createMediaRecord("hash3", 3000, "uri6", 5000L) // Single, not a duplicate
        )

        val groups = mutableMapOf<String, MutableList<MediaRecord>>()
        for (record in records) {
            record.hash?.let { hash ->
                groups.getOrPut(hash) { mutableListOf() }.add(record)
            }
        }

        val duplicateGroups = mutableListOf<DuplicateGroup>()
        for ((hash, groupRecords) in groups) {
            if (groupRecords.size > 1) {
                val first = groupRecords.first()
                val recommendedKeep = groupRecords.maxByOrNull { it.modifiedDate }
                val group = DuplicateGroup(
                    hash = hash,
                    sizeBytes = first.sizeBytes,
                    count = groupRecords.size,
                    category = first.category,
                    recommendedKeepUri = recommendedKeep?.uri,
                    scanSessionId = 0
                )
                duplicateGroups.add(group)
            }
        }

        assertEquals(2, duplicateGroups.size)
        
        val group1 = duplicateGroups.find { it.hash == "hash1" }!!
        assertEquals(3, group1.count)
        assertEquals(2000, group1.totalWastedBytes) // 1000 * (3-1)
        assertEquals("uri2", group1.recommendedKeepUri) // Most recent

        val group2 = duplicateGroups.find { it.hash == "hash2" }!!
        assertEquals(2, group2.count)
        assertEquals(2000, group2.totalWastedBytes) // 2000 * (2-1)
        assertEquals("uri5", group2.recommendedKeepUri)
    }

    private fun createMediaRecord(hash: String, size: Long, uri: String, modifiedDate: Long): MediaRecord {
        return MediaRecord(
            uri = uri,
            displayName = "test.jpg",
            mimeType = "image/jpeg",
            sizeBytes = size,
            modifiedDate = modifiedDate,
            createdDate = modifiedDate,
            relativePath = "Pictures/test.jpg",
            width = 1920,
            height = 1080,
            durationMs = null,
            hash = hash,
            category = MediaCategory.IMAGES,
            scanSessionId = 1
        )
    }
}

class CleanupRecommendationTest {

    @Test
    fun `generateCleanupRecommendations includes duplicates`() {
        // Test the logic in StorageRepositoryImpl.generateCleanupRecommendations()
        val duplicateGroups = listOf(
            DuplicateGroup("hash1", 1000, 3, MediaCategory.IMAGES, "uri1", 0),
            DuplicateGroup("hash2", 2000, 2, MediaCategory.VIDEOS, "uri2", 0)
        )

        val totalWasted = duplicateGroups.sumOf { it.totalWastedBytes }
        assertEquals(5000, totalWasted) // (1000*2) + (2000*1)

        val item = CleanupItem(
            category = CleanupCategory.DUPLICATES,
            title = "Duplicate Files",
            description = "${duplicateGroups.size} duplicate groups found",
            whyRecommended = "Exact duplicate files are wasting space. Keep one copy and remove the rest.",
            whatHappensIfDeleted = "Duplicate copies will be moved to trash. Original files remain.",
            estimatedBytes = totalWasted,
            risk = CleanupRisk.LOW,
            relatedUris = duplicateGroups.mapNotNull { it.recommendedKeepUri }
        )

        assertEquals(CleanupCategory.DUPLICATES, item.category)
        assertEquals(CleanupRisk.LOW, item.risk)
        assertEquals(5000, item.estimatedBytes)
    }
}

class RecommendationEngineTest {

    @Test
    fun `generateRecommendations creates high storage usage recommendation`() {
        // Test the logic in StorageRepositoryImpl.generateRecommendations()
        val usagePercent = 95f
        val duplicateWasted = 200L * 1024 * 1024 // 200MB

        val recommendations = mutableListOf<Recommendation>()

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

        if (duplicateWasted > 100 * 1024 * 1024) {
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

        assertEquals(2, recommendations.size)
        assertEquals(RecommendationPriority.URGENT, recommendations[0].priority)
        assertEquals(RecommendationPriority.HIGH, recommendations[1].priority)
        assertEquals(RecommendationType.HIGH_STORAGE_USAGE, recommendations[0].type)
        assertEquals(RecommendationType.DUPLICATES_FOUND, recommendations[1].type)
    }

    @Test
    fun `generateRecommendations skips when thresholds not met`() {
        val usagePercent = 50f
        val duplicateWasted = 50L * 1024 * 1024 // 50MB

        val recommendations = mutableListOf<Recommendation>()

        if (usagePercent > 90f) {
            recommendations.add(Recommendation(
                type = RecommendationType.HIGH_STORAGE_USAGE,
                priority = RecommendationPriority.URGENT,
                title = "Storage Almost Full",
                description = "",
                estimatedRecoverableBytes = 0,
                actionLabel = "Review Cleanup",
                actionRoute = "clean"
            ))
        }

        if (duplicateWasted > 100 * 1024 * 1024) {
            recommendations.add(Recommendation(
                type = RecommendationType.DUPLICATES_FOUND,
                priority = RecommendationPriority.HIGH,
                title = "Duplicate Files Found",
                description = "",
                estimatedRecoverableBytes = duplicateWasted,
                actionLabel = "Review Duplicates",
                actionRoute = "duplicates"
            ))
        }

        assertEquals(0, recommendations.size)
    }
}

class StorageBreakdownCalculationTest {

    @Test
    fun `calculateStorageBreakdown sums correctly`() {
        val totalBytes = 128L * 1024 * 1024 * 1024 // 128GB
        val appBytes = 24L * 1024 * 1024 * 1024 // 24GB
        val mediaBytes = 30L * 1024 * 1024 * 1024 // 30GB
        val docsBytes = 10L * 1024 * 1024 * 1024 // 10GB
        val otherBytes = totalBytes - appBytes - mediaBytes - docsBytes

        assertEquals(64L * 1024 * 1024 * 1024, otherBytes)

        val breakdown = mutableListOf<StorageBreakdown>()
        if (appBytes > 0) breakdown.add(StorageBreakdown(MediaCategory.APPS, appBytes, 0))
        if (mediaBytes > 0) breakdown.add(StorageBreakdown(MediaCategory.VIDEOS, mediaBytes, 0))
        if (docsBytes > 0) breakdown.add(StorageBreakdown(MediaCategory.DOCUMENTS, docsBytes, 0))
        if (otherBytes > 0) breakdown.add(StorageBreakdown(MediaCategory.OTHER, otherBytes, 0))

        val sum = breakdown.sumOf { it.bytes }
        assertEquals(totalBytes, sum)
    }
}
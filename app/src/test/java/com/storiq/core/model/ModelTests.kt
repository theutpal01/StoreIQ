package com.storiq.core.model

import org.junit.Test
import org.junit.Assert.*

class StorageBreakdownTest {

    @Test
    fun `formatBytes formats bytes correctly`() {
        assertEquals("0 B", StorageBreakdown.formatBytes(0))
        assertEquals("512 B", StorageBreakdown.formatBytes(512))
        assertEquals("1.00 KB", StorageBreakdown.formatBytes(1024))
        assertEquals("1.50 KB", StorageBreakdown.formatBytes(1536))
        assertEquals("1.00 MB", StorageBreakdown.formatBytes(1024 * 1024))
        assertEquals("1.00 GB", StorageBreakdown.formatBytes(1024 * 1024 * 1024))
        assertEquals("1.00 TB", StorageBreakdown.formatBytes(1024L * 1024 * 1024 * 1024))
    }

    @Test
    fun `StorageBreakdown creates correct formatted size`() {
        val breakdown = StorageBreakdown(MediaCategory.IMAGES, 1024 * 1024 * 10, 5)
        assertEquals("10.00 MB", breakdown.formattedSize)
    }
}

class StorageSnapshotTest {

    @Test
    fun `usagePercent calculates correctly`() {
        val snapshot = StorageSnapshot(
            totalBytes = 1000,
            usedBytes = 500,
            freeBytes = 500,
            appBytes = 200,
            mediaBytes = 150,
            documentsBytes = 100,
            otherBytes = 50
        )
        assertEquals(50f, snapshot.usagePercent, 0.001f)
    }

    @Test
    fun `usagePercent handles zero total`() {
        val snapshot = StorageSnapshot(
            totalBytes = 0,
            usedBytes = 0,
            freeBytes = 0,
            appBytes = 0,
            mediaBytes = 0,
            documentsBytes = 0,
            otherBytes = 0
        )
        assertEquals(0f, snapshot.usagePercent, 0.001f)
    }
}

class MediaRecordTest {

    @Test
    fun `formattedSize returns correct format`() {
        val record = MediaRecord(
            uri = "content://media/1",
            displayName = "test.jpg",
            mimeType = "image/jpeg",
            sizeBytes = 1024 * 1024 * 5,
            modifiedDate = System.currentTimeMillis(),
            createdDate = System.currentTimeMillis(),
            relativePath = "Pictures/test.jpg",
            width = 1920,
            height = 1080,
            durationMs = null,
            hash = "abc123",
            category = MediaCategory.IMAGES,
            scanSessionId = 1
        )
        assertEquals("5.00 MB", record.formattedSize)
    }
}

class AppRecordTest {

    @Test
    fun `isUnused returns true for apps not used in 90 days`() {
        val oldTimestamp = System.currentTimeMillis() - (100L * 24 * 60 * 60 * 1000)
        val record = AppRecord(
            packageName = "com.test.app",
            appName = "Test App",
            totalBytes = 1024 * 1024 * 100,
            appBytes = 1024 * 1024 * 50,
            userDataBytes = 1024 * 1024 * 50,
            cacheBytes = 0,
            installDate = System.currentTimeMillis() - (200L * 24 * 60 * 60 * 1000),
            lastUsedTimestamp = oldTimestamp,
            isSystemApp = false
        )
        assertTrue(record.isUnused)
    }

    @Test
    fun `isUnused returns false for recently used apps`() {
        val recentTimestamp = System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000)
        val record = AppRecord(
            packageName = "com.test.app",
            appName = "Test App",
            totalBytes = 1024 * 1024 * 100,
            appBytes = 1024 * 1024 * 50,
            userDataBytes = 1024 * 1024 * 50,
            cacheBytes = 0,
            installDate = System.currentTimeMillis() - (200L * 24 * 60 * 60 * 1000),
            lastUsedTimestamp = recentTimestamp,
            isSystemApp = false
        )
        assertFalse(record.isUnused)
    }

    @Test
    fun `isUnused returns true for null lastUsedTimestamp`() {
        val record = AppRecord(
            packageName = "com.test.app",
            appName = "Test App",
            totalBytes = 1024 * 1024 * 100,
            appBytes = 1024 * 1024 * 50,
            userDataBytes = 1024 * 1024 * 50,
            cacheBytes = 0,
            installDate = System.currentTimeMillis() - (200L * 24 * 60 * 60 * 1000),
            lastUsedTimestamp = null,
            isSystemApp = false
        )
        assertTrue(record.isUnused)
    }

    @Test
    fun `daysSinceLastUsed calculates correctly`() {
        val timestamp = System.currentTimeMillis() - (50L * 24 * 60 * 60 * 1000)
        val record = AppRecord(
            packageName = "com.test.app",
            appName = "Test App",
            totalBytes = 1024 * 1024 * 100,
            appBytes = 1024 * 1024 * 50,
            userDataBytes = 1024 * 1024 * 50,
            cacheBytes = 0,
            installDate = System.currentTimeMillis() - (200L * 24 * 60 * 60 * 1000),
            lastUsedTimestamp = timestamp,
            isSystemApp = false
        )
        assertEquals(50, record.daysSinceLastUsed)
    }

    @Test
    fun `daysSinceLastUsed returns null for null timestamp`() {
        val record = AppRecord(
            packageName = "com.test.app",
            appName = "Test App",
            totalBytes = 1024 * 1024 * 100,
            appBytes = 1024 * 1024 * 50,
            userDataBytes = 1024 * 1024 * 50,
            cacheBytes = 0,
            installDate = System.currentTimeMillis() - (200L * 24 * 60 * 60 * 1000),
            lastUsedTimestamp = null,
            isSystemApp = false
        )
        assertNull(record.daysSinceLastUsed)
    }
}

class DuplicateGroupTest {

    @Test
    fun `totalWastedBytes calculates correctly`() {
        val group = DuplicateGroup(
            hash = "abc123",
            sizeBytes = 1024 * 1024 * 10,
            count = 3,
            category = MediaCategory.IMAGES,
            recommendedKeepUri = "content://media/1",
            scanSessionId = 1
        )
        assertEquals(1024L * 1024 * 10 * 2, group.totalWastedBytes)
        assertEquals("20.00 MB", group.formattedWastedSize)
    }

    @Test
    fun `totalWastedBytes is zero for single file`() {
        val group = DuplicateGroup(
            hash = "abc123",
            sizeBytes = 1024 * 1024 * 10,
            count = 1,
            category = MediaCategory.IMAGES,
            recommendedKeepUri = "content://media/1",
            scanSessionId = 1
        )
        assertEquals(0L, group.totalWastedBytes)
    }
}

class CleanupItemTest {

    @Test
    fun `formattedSize returns correct format`() {
        val item = CleanupItem(
            category = CleanupCategory.DUPLICATES,
            title = "Duplicates",
            description = "Test",
            whyRecommended = "Test",
            whatHappensIfDeleted = "Test",
            estimatedBytes = 1024 * 1024 * 100,
            risk = CleanupRisk.LOW,
            relatedUris = listOf("uri1", "uri2")
        )
        assertEquals("100.00 MB", item.formattedSize)
    }
}

class RecommendationTest {

    @Test
    fun `formattedRecoverableSize returns correct format`() {
        val recommendation = Recommendation(
            type = RecommendationType.DUPLICATES_FOUND,
            priority = RecommendationPriority.HIGH,
            title = "Duplicates",
            description = "Test",
            estimatedRecoverableBytes = 1024 * 1024 * 500,
            actionLabel = "Review",
            actionRoute = "duplicates"
        )
        assertEquals("500.00 MB", recommendation.formattedRecoverableSize)
    }
}
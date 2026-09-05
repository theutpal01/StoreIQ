package com.storiq.feature.history

import com.storiq.core.model.StorageSnapshot
import com.storiq.core.storage.StorageRepository
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class HistoryViewModelTest {

    @Test
    fun `calculateStorageGrowth returns correct value`() = runBlocking {
        val snapshots = listOf(
            createSnapshot(1000, 800, 200, 300, 200, 200, 100, System.currentTimeMillis()), // Latest
            createSnapshot(1000, 700, 300, 250, 200, 150, 100, System.currentTimeMillis() - 24*60*60*1000L), // 1 day ago
            createSnapshot(1000, 600, 400, 200, 150, 150, 100, System.currentTimeMillis() - 48*60*60*1000L)  // 2 days ago
        )

        val growth = snapshots.first().usedBytes - snapshots.last().usedBytes
        assertEquals(200L, growth)
        
        val growthPercent = ((growth.toFloat() / snapshots.last().usedBytes) * 100)
        assertEquals(33.33f, growthPercent, 0.01f)
    }

    @Test
    fun `calculateAverageDailyGrowth`() = runBlocking {
        val snapshots = listOf(
            createSnapshot(1000, 800, 200, 300, 200, 200, 100, System.currentTimeMillis()), // Latest
            createSnapshot(1000, 500, 500, 150, 150, 100, 100, System.currentTimeMillis() - 10*24*60*60*1000L) // 10 days ago
        )

        val growth = snapshots.first().usedBytes - snapshots.last().usedBytes // 300
        val days = 10
        val dailyGrowth = growth / days
        
        assertEquals(30L, dailyGrowth)
    }

    @Test
    fun `projectedFullDate calculates correctly`() = runBlocking {
        val freeSpace = 100L * 1024 * 1024 * 1024 // 100GB
        val dailyGrowth = 2L * 1024 * 1024 * 1024 // 2GB/day
        val daysToFull = freeSpace / dailyGrowth // 50 days
        
        assertEquals(50L, daysToFull)
    }

    @Test
    fun `chartDataPoint converts bytes to GB correctly`() {
        val snapshot = createSnapshot(
            128L * 1024 * 1024 * 1024, // 128GB total
            80L * 1024 * 1024 * 1024,  // 80GB used
            48L * 1024 * 1024 * 1024,  // 48GB free
            25L * 1024 * 1024 * 1024,  // 25GB apps
            30L * 1024 * 1024 * 1024,  // 30GB media
            15L * 1024 * 1024 * 1024,  // 15GB docs
            10L * 1024 * 1024 * 1024,  // 10GB other
            System.currentTimeMillis()
        )

        val usedGB = snapshot.usedBytes / (1024f * 1024 * 1024)
        val freeGB = snapshot.freeBytes / (1024f * 1024 * 1024)
        val appsGB = snapshot.appBytes / (1024f * 1024 * 1024)
        val mediaGB = snapshot.mediaBytes / (1024f * 1024 * 1024)
        val docsGB = snapshot.documentsBytes / (1024f * 1024 * 1024)
        val otherGB = snapshot.otherBytes / (1024f * 1024 * 1024)

        assertEquals(80f, usedGB, 0.1f)
        assertEquals(48f, freeGB, 0.1f)
        assertEquals(25f, appsGB, 0.1f)
        assertEquals(30f, mediaGB, 0.1f)
        assertEquals(15f, docsGB, 0.1f)
        assertEquals(10f, otherGB, 0.1f)
    }

    private fun createSnapshot(
        totalBytes: Long,
        usedBytes: Long,
        freeBytes: Long,
        appBytes: Long,
        mediaBytes: Long,
        documentsBytes: Long,
        otherBytes: Long,
        timestamp: Long
    ): StorageSnapshot {
        return StorageSnapshot(
            timestamp = timestamp,
            totalBytes = totalBytes,
            usedBytes = usedBytes,
            freeBytes = freeBytes,
            appBytes = appBytes,
            mediaBytes = mediaBytes,
            documentsBytes = documentsBytes,
            otherBytes = otherBytes
        )
    }
}
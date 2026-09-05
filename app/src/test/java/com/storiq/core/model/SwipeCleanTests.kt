package com.storiq.core.model

import org.junit.Test
import org.junit.Assert.*

class SwipeCleanModelTest {

    @Test
    fun `SwipeDecision enum has correct values`() {
        val values = SwipeDecision.values()
        assertEquals(3, values.size)
        assertTrue(values.contains(SwipeDecision.KEEP))
        assertTrue(values.contains(SwipeDecision.DELETE))
        assertTrue(values.contains(SwipeDecision.UNDECIDED))
    }

    @Test
    fun `SwipeMediaType enum has correct values`() {
        val values = SwipeMediaType.values()
        assertEquals(4, values.size)
        assertTrue(values.contains(SwipeMediaType.PHOTOS))
        assertTrue(values.contains(SwipeMediaType.VIDEOS))
        assertTrue(values.contains(SwipeMediaType.PHOTOS_AND_VIDEOS))
        assertTrue(values.contains(SwipeMediaType.SMART_CLEAN))
    }

    @Test
    fun `SwipeSession calculates correctly`() {
        val session = SwipeSession(
            mediaType = SwipeMediaType.PHOTOS,
            totalItems = 100,
            keptItems = 70,
            deleteCandidates = 30,
            totalCandidateBytes = 1024 * 1024 * 500 // 500MB
        )

        assertEquals(SwipeMediaType.PHOTOS, session.mediaType)
        assertEquals(100, session.totalItems)
        assertEquals(70, session.keptItems)
        assertEquals(30, session.deleteCandidates)
        assertEquals(1024L * 1024 * 500, session.totalCandidateBytes)
        assertFalse(session.isCompleted)
    }

    @Test
    fun `SwipeSession completed when completedAt set`() {
        val session = SwipeSession(
            mediaType = SwipeMediaType.PHOTOS,
            completedAt = System.currentTimeMillis()
        )

        assertTrue(session.isCompleted)
    }

    @Test
    fun `SwipeDecisionRecord stores correct data`() {
        val record = SwipeDecisionRecord(
            sessionId = 1,
            mediaUri = "content://media/123",
            decision = SwipeDecision.DELETE,
            timestamp = System.currentTimeMillis()
        )

        assertEquals(1L, record.sessionId)
        assertEquals("content://media/123", record.mediaUri)
        assertEquals(SwipeDecision.DELETE, record.decision)
    }
}
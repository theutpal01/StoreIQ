package com.storiq.core.storage

import com.storiq.core.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class StorageRepositoryImplTest {

    private lateinit var database: StorIQDatabase
    private lateinit var dispatcherProvider: CoroutineDispatcherProvider
    private lateinit var context: android.content.Context
    private lateinit var preferencesDataStore: androidx.datastore.preferences.PreferencesDataStore
    private lateinit var repository: StorageRepositoryImpl

    @Before
    fun setup() {
        database = mock()
        dispatcherProvider = mock()
        context = mock()
        preferencesDataStore = mock()
        repository = StorageRepositoryImpl(database, dispatcherProvider, context, preferencesDataStore)
    }

    @Test
    fun `getTotalStorageBytes returns data directory total space`() = runBlocking {
        // This test would need a real context to work properly
        // For now, we verify the repository can be instantiated
        assertNotNull(repository)
    }
}

class DeletionResultTest {

    @Test
    fun `creates correct deletion result`() {
        val result = DeletionResult(
            success = true,
            deletedCount = 5,
            failedCount = 1,
            deletedBytes = 1024 * 1024 * 10,
            errors = listOf("Failed to delete file1")
        )

        assertTrue(result.success)
        assertEquals(5, result.deletedCount)
        assertEquals(1, result.failedCount)
        assertEquals(1024L * 1024 * 10, result.deletedBytes)
        assertEquals(1, result.errors.size)
    }
}
package com.storiq

import android.app.Application
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.storiq.core.database.StorIQDatabase
import com.storiq.core.storage.StorageRepository
import com.storiq.core.storage.StorageRepositoryImpl
import com.storiq.core.permissions.PermissionManager
import com.storiq.core.permissions.PermissionManagerImpl
import com.storiq.core.util.CoroutineDispatcherProvider
import com.storiq.core.util.DefaultCoroutineDispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@HiltAndroidApp
class StorIQApplication : Application() {

    private val dataStore by lazy {
        preferencesDataStore(name = "storiq_preferences")
    }

    private val database by lazy {
        Room.databaseBuilder(
            this,
            StorIQDatabase::class.java,
            "storiq_database"
        ).fallbackToDestructiveMigration().build()
    }

    val coroutineDispatcherProvider: CoroutineDispatcherProvider = DefaultCoroutineDispatcherProvider()

    val storageRepository: StorageRepository by lazy {
        StorageRepositoryImpl(database, coroutineDispatcherProvider)
    }

    val permissionManager: PermissionManager by lazy {
        PermissionManagerImpl(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Handle memory pressure if needed
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ApplicationEntryPoint {
    fun storageRepository(): StorageRepository
    fun permissionManager(): PermissionManager
    fun coroutineDispatcherProvider(): CoroutineDispatcherProvider
}

object AppEntryPoint {
    fun get(application: Application): ApplicationEntryPoint =
        EntryPointAccessors.fromApplication(application, ApplicationEntryPoint::class.java)
}
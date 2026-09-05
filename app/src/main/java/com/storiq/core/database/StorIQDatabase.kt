package com.storiq.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.storiq.core.model.*
import com.storiq.core.database.converters.Converters

@Database(
    entities = [
        StorageSnapshot::class,
        MediaRecord::class,
        FileRecord::class,
        AppRecord::class,
        DuplicateGroup::class,
        CleanupItem::class,
        Recommendation::class,
        ScanSession::class,
        SwipeSession::class,
        SwipeDecisionRecord::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StorIQDatabase : RoomDatabase() {
    abstract fun storageSnapshotDao(): StorageSnapshotDao
    abstract fun mediaRecordDao(): MediaRecordDao
    abstract fun fileRecordDao(): FileRecordDao
    abstract fun appRecordDao(): AppRecordDao
    abstract fun duplicateGroupDao(): DuplicateGroupDao
    abstract fun cleanupItemDao(): CleanupItemDao
    abstract fun recommendationDao(): RecommendationDao
    abstract fun scanSessionDao(): ScanSessionDao
    abstract fun swipeSessionDao(): SwipeSessionDao
    abstract fun swipeDecisionRecordDao(): SwipeDecisionRecordDao

    companion object {
        @Volatile
        private var INSTANCE: StorIQDatabase? = null

        fun getDatabase(context: Context): StorIQDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StorIQDatabase::class.java,
                    "storiq_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
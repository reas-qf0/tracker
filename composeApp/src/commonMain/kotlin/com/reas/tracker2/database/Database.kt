package com.reas.tracker2.database

import androidx.room.*
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.reas.tracker2.database.daos.*
import com.reas.tracker2.database.entities.*
import kotlinx.coroutines.Dispatchers

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

@Database(entities = [
    EventEntity::class,
    PlayEntity::class,
    ProcessingQueueEntity::class,
    SyncQueueEntity::class,
    ApiKeyEntity::class,
], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun playDao(): PlayDao
    abstract fun processingQueueDao(): ProcessingQueueDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun apiKeyDao(): ApiKeyDao

    companion object {
        fun getDatabase(builder: Builder<AppDatabase>) : AppDatabase {
            return builder
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .fallbackToDestructiveMigration(false)
                .build()
        }
    }
}

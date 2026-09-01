package com.reas.tracker2.database

import androidx.room3.*
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
    SyncQueueEntity::class,
    ApiKeyEntity::class,
    TrackEntity::class,
    AlbumEntity::class,
    ArtistEntity::class,
    TrackArtistCrossRef::class,
    AlbumArtistCrossRef::class,
], version = 1, exportSchema = false)
@ColumnTypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun playDao(): PlayDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun apiKeyDao(): ApiKeyDao
    abstract fun trackDao(): TrackDao

    companion object {
        fun getDatabase(builder: Builder<AppDatabase>) : AppDatabase {
            return builder
                .setQueryCoroutineContext(Dispatchers.IO)
                .fallbackToDestructiveMigration(false)
                .build()
        }
    }
}

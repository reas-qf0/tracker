package com.reas.tracker2.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.reas.tracker2.database.objects.Event
import com.reas.tracker2.database.daos.EventDao
import com.reas.tracker2.database.objects.Play
import com.reas.tracker2.database.daos.PlayDao
import kotlinx.coroutines.Dispatchers

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

@Database(entities = [Event::class, Play::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun playDao(): PlayDao

    companion object {
        fun getDatabase(builder: Builder<AppDatabase>) : AppDatabase {
            return builder
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
        }
    }
}

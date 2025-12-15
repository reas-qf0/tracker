package org.reas.tracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Event::class, Play::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventsDao(): EventsDao
    abstract fun playDao(): PlayDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

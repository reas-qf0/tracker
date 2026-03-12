package com.reas.tracker2.database

import androidx.room.Room
import androidx.room.RoomDatabase
import co.touchlab.kermit.Logger
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(/*System.getProperty("java.io.tmpdir"), */"client.db")
    Logger.d("Database") { "Database file: $dbFile" }
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
}
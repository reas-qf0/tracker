package com.reas.tracker2.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import co.touchlab.kermit.Logger

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("database")
    Logger.d("Database") { "Database file: $dbFile" }
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
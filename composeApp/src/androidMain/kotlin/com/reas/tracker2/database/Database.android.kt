package com.reas.tracker2.database

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.reas.tracker2.util.PlatformDependentPaths

fun getDatabaseBuilder(context: Context, pathProvider: PlatformDependentPaths): RoomDatabase.Builder<AppDatabase> {
    val dbFile = pathProvider.getDatabasePath()
    return Room.databaseBuilder<AppDatabase>(
        context = context.applicationContext,
        name = dbFile
    ).setDriver(AndroidSQLiteDriver())
}
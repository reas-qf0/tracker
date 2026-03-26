package com.reas.tracker2.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.reas.tracker2.util.PlatformDependentPaths

fun getDatabaseBuilder(context: Context, pathProvider: PlatformDependentPaths): RoomDatabase.Builder<AppDatabase> {
    val dbFile = pathProvider.getDatabasePath()
    return Room.databaseBuilder<AppDatabase>(
        context = context.applicationContext,
        name = dbFile
    )
}
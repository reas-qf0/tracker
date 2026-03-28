package com.reas.tracker2.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.reas.tracker2.util.PlatformDependentPaths
import io.github.oshai.kotlinlogging.KotlinLogging

private object Database {
    val logger = KotlinLogging.logger {}
}

fun getDatabaseBuilder(pathProvider: PlatformDependentPaths): RoomDatabase.Builder<AppDatabase> {
    val dbFile = pathProvider.getDatabasePath()
    Database.logger.debug { "Database file: $dbFile" }
    return Room.databaseBuilder<AppDatabase>(name = dbFile)
        .setDriver(BundledSQLiteDriver())
}
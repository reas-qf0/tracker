package com.reas.tracker2.database

import com.reas.tracker2.database.tables.ApiKeyTable
import com.reas.tracker2.database.tables.EventTable
import com.reas.tracker2.database.tables.PlayTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun createInMemoryDatabase(): Database {
    val database = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = "",
    )
    transaction(database) {
        SchemaUtils.create(EventTable, PlayTable, ApiKeyTable)
    }
    return database
}

fun createSQLiteDatabase(): Database {
    val database = Database.connect(
        url = "jdbc:sqlite:server.db",
        driver = "org.sqlite.JDBC"
    )
    transaction(database) {
        SchemaUtils.create(EventTable, PlayTable, ApiKeyTable)
    }
    return database
}
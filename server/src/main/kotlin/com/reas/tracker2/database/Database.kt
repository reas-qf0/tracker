package com.reas.tracker2.database

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
        SchemaUtils.create(EventTable)
        SchemaUtils.create(PlayTable)
    }
    return database
}

fun createSQLiteDatabase(): Database {
    val database = Database.connect(
        url = "jdbc:sqlite:data.db",
        driver = "org.sqlite.JDBC"
    )
    transaction(database) {
        SchemaUtils.create(EventTable)
        SchemaUtils.create(PlayTable)
    }
    return database
}
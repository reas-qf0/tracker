package com.reas.tracker2.database.tables

import org.jetbrains.exposed.v1.core.Table

object EventTable : Table("events") {
    val trackId = reference("trackId", TrackTable.id)
    val timestamp = long("timestamp")
    val position = long("position")
    val duration = long("duration")
    val state = text("state")
    val sourceUser = varchar("source_user", 64)
    val sourceDevice = varchar("source_device", 64)
    val sourceApp = text("source_app")

    override val primaryKey = PrimaryKey(sourceUser, sourceDevice, sourceApp, timestamp)
}
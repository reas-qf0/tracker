package com.reas.tracker2.database.tables

import org.jetbrains.exposed.v1.core.Table

object PlayTable : Table("plays") {
    val trackId = reference("trackId", TrackTable.id)
    val timestamp = long("timestamp")
    val duration = long("duration")
    val timePlayed = long("time_played")
    val lastPosition = long("last_position")
    val lastPlaying = bool("last_playing")
    val sourceUser = varchar("source_user", 64)
    val sourceDevice = varchar("source_device", 64)
    val sourceApp = text("source_app")
    val associatedEvents = text("associated_events")
    val id = long("id")

    override val primaryKey = PrimaryKey(sourceUser, sourceDevice, sourceApp, timestamp)
}
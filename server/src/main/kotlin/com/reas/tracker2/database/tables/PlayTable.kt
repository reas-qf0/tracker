package com.reas.tracker2.database.tables

import org.jetbrains.exposed.v1.core.Table

object PlayTable : Table("plays") {
    val track = text("track")
    val artist = text("artist")
    val album = text("album").nullable()
    val albumArtist = text("album_artist").nullable()
    val timestamp = long("timestamp")
    val duration = long("duration")
    val timePlayed = long("position")
    val lastPosition = long("last_position")
    val lastPlaying = bool("last_playing")
    val sourceUser = varchar("source_user", 64)
    val sourceDevice = varchar("source_device", 64)
    val sourceApp = text("source_app")
    val associatedEvents = text("associated_events")

    override val primaryKey = PrimaryKey(sourceUser, sourceDevice, sourceApp, timestamp)
}
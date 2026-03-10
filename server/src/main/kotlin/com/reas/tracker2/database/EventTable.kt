package com.reas.tracker2.database

import org.jetbrains.exposed.v1.core.Table

object EventTable : Table("events") {
    val track = text("track")
    val artist = text("artist")
    val album = text("album").nullable()
    val albumArtist = text("album_artist").nullable()
    val timestamp = long("timestamp")
    val position = long("position")
    val duration = long("duration")
    val isPlaying = bool("is_playing")
    val sourceUser = varchar("source_user", 64)
    val sourceDevice = varchar("source_device", 64)
    val sourceApp = text("source_app")

    override val primaryKey = PrimaryKey(sourceUser, sourceDevice, sourceApp, timestamp)
}
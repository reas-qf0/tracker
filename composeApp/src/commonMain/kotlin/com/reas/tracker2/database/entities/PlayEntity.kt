package com.reas.tracker2.database.entities

import androidx.room.Entity
import com.reas.tracker2.shared.Play
import com.reas.tracker2.shared.Source

@Entity(tableName = "plays", primaryKeys = ["sourceDevice", "sourceApp", "timestamp"])
data class PlayEntity(
    val track: String,
    val artist: String,
    val album: String?,
    val albumArtist: String,
    val timestamp: Long,
    val duration: Long,
    var timePlayed: Long,
    var lastPosition: Long,
    var lastPlaying: Boolean,
    val sourceDevice: String,
    val sourceApp: String,
    val associatedEvents: MutableList<Play.EventInfo?>
) {
    fun toObject() = Play.create(
        track = track,
        artist = artist,
        album = album,
        albumArtist = albumArtist,
        timestamp = timestamp,
        duration = duration,
        timePlayed = timePlayed,
        source = Source.user(app = sourceApp, device = sourceDevice),
        associatedEvents = associatedEvents
    )

    companion object {
        fun Play.toEntity() = PlayEntity(
            track = track,
            artist = artist,
            album = album,
            albumArtist = albumArtist ?: artist,
            timestamp = timestamp.toEpochMilliseconds(),
            duration = duration.inWholeMilliseconds,
            timePlayed = timePlayed.inWholeMilliseconds,
            sourceDevice = sourceDevice,
            sourceApp = sourceApp,
            lastPlaying = lastPlaying,
            lastPosition = lastPosition.inWholeMilliseconds,
            associatedEvents = associatedEvents
        )
    }
}
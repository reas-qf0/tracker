package com.reas.tracker2.database.entities

import androidx.room.Entity
import com.reas.tracker2.shared.Album
import com.reas.tracker2.shared.Metadata
import com.reas.tracker2.shared.Play
import com.reas.tracker2.shared.Source
import com.reas.tracker2.shared.Track
import com.reas.tracker2.shared.TrackWithOptionalAlbum
import kotlinx.serialization.json.Json
import kotlin.time.DurationUnit
import kotlin.time.Instant
import kotlin.time.toDuration

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
    fun toObject() = Play(
        metadata = Metadata(
            info = TrackWithOptionalAlbum(
                _track = Track(
                    title = track,
                    artist = artist
                ),
                _album = if (album != null) Album(
                    title = album,
                    artist = albumArtist
                ) else null
            ),
            duration = duration.toDuration(DurationUnit.MILLISECONDS),
        ),
        timestamp = Instant.fromEpochMilliseconds(timestamp),
        timePlayed = timePlayed.toDuration(DurationUnit.MILLISECONDS),
        source = Source(app = sourceApp, device = sourceDevice),
        associatedEvents = associatedEvents,
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
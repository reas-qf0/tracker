package com.reas.tracker2.database.entities

import androidx.room.Entity
import com.reas.tracker2.shared.*
import kotlinx.serialization.Serializable
import kotlin.time.DurationUnit
import kotlin.time.Instant
import kotlin.time.toDuration

@Serializable
@Entity(tableName = "events", primaryKeys = ["sourceApp", "timestamp"])
data class EventEntity(
    val track: String,
    val artist: String,
    val album: String?,
    val albumArtist: String,
    val timestamp: Long,
    val position: Long,
    val duration: Long,
    val isPlaying: Boolean,
    val sourceApp: String
) {
    fun toObject() = Event(
        metadata = Metadata(
            info = TrackWithOptionalAlbum(
                _track = Track(
                    title = track,
                    artist = artist
                ),
                _album = if (album != null) Album(
                    title = album,
                    artist = albumArtist
                ) else null,
            ),
            duration = duration.toDuration(DurationUnit.MILLISECONDS),
        ),
        timestamp = Instant.fromEpochMilliseconds(timestamp),
        isPlaying = isPlaying,
        position = position.toDuration(DurationUnit.MILLISECONDS),
        source = Source.local(sourceApp)
    )

    companion object {
        fun Event.toEntity() = EventEntity(
            track = track,
            artist = artist,
            album = album,
            albumArtist = albumArtist ?: artist,
            duration = duration.inWholeMilliseconds,
            isPlaying = isPlaying,
            sourceApp = sourceApp,
            timestamp = timestamp.toEpochMilliseconds(),
            position = position.inWholeMilliseconds
        )
    }
}
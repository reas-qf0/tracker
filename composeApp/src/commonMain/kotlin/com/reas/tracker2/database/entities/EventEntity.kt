package com.reas.tracker2.database.entities

import androidx.room.Entity
import com.reas.tracker2.shared.Event
import com.reas.tracker2.shared.Source
import kotlinx.serialization.Serializable

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
    fun toObject() = Event.create(
        track = track,
        artist = artist,
        album = album,
        albumArtist = albumArtist,
        timestamp = timestamp,
        position = position,
        duration = duration,
        isPlaying = isPlaying,
        source = Source.local(sourceApp),
    )

    companion object {
        fun Event.toEntity() = EventEntity(
            track = track,
            artist = artist,
            album = album,
            albumArtist = albumArtist ?: artist,
            duration = duration.inWholeMilliseconds,
            isPlaying = isPlaying,
            sourceApp = app,
            timestamp = timestamp.toEpochMilliseconds(),
            position = position.inWholeMilliseconds
        )
    }
}
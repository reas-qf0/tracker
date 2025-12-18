package org.reas.tracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(tableName = "events")
data class Event @OptIn(ExperimentalUuidApi::class) constructor(
    val track: String,
    val artist: String,
    val album: String?,
    val albumArtist: String,
    val playerId: String,
    val timestamp: Long,
    val position: Long,
    val duration: Long,
    val isPlaying: Boolean,
    @PrimaryKey(autoGenerate = false)
    val id: String = Uuid.random().toHexDashString()
) {
    fun isEqual(other: Event): Boolean =
        track == other.track &&
        artist == other.artist &&
        album == other.album &&
        albumArtist == other.albumArtist &&
        duration == other.duration &&
        timestamp == other.timestamp &&
        position == other.position &&
        isPlaying == other.isPlaying &&
        playerId == other.playerId

    fun metadataEqual(other: Play): Boolean =
        track == other.track &&
        artist == other.artist &&
        album == other.album &&
        albumArtist == other.albumArtist &&
        duration == other.duration

    fun asMap() = hashMapOf<String, Any?>(
        "track" to track,
        "artist" to artist,
        "album" to album,
        "album_artist" to albumArtist,
        "app" to playerId,
        "timestamp" to timestamp,
        "position" to position,
        "duration" to duration,
        "is_playing" to isPlaying,
        "id" to id,
    )

    companion object {
        fun fromMap(map: Map<String, Any?>) = Event(
            track = map["track"] as String,
            artist = map["artist"] as String,
            album = map["album"] as String?,
            albumArtist = map["album_artist"] as String,
            playerId = map["app"] as String,
            timestamp = map["timestamp"] as Long,
            position = map["position"] as Long,
            duration = map["duration"] as Long,
            isPlaying = map["is_playing"] as Boolean,
            id = map["id"] as String
        )
    }
}
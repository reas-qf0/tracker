package org.reas.tracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    val track: String,
    val artist: String,
    val album: String?,
    val albumArtist: String?,
    val app: String,
    val timestamp: Long,
    val position: Long,
    val duration: Long,
    val isPlaying: Boolean,
) {
    @PrimaryKey(autoGenerate = true) var id: Long = 0

    fun metadataEqual(other: Event): Boolean =
        track == other.track &&
        artist == other.artist &&
        album == other.album &&
        albumArtist == other.albumArtist &&
        duration == other.duration

    fun metadataEqual(other: Play): Boolean =
        track == other.track &&
        artist == other.artist &&
        album == other.album &&
        albumArtist == other.albumArtist &&
        duration == other.duration
}
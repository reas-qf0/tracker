package com.reas.tracker2.database.objects

import androidx.room.Entity

@Entity(tableName = "events", primaryKeys = ["sourceApp", "timestamp"])
data class Event(
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
    fun metadataEqual(other: Play): Boolean =
        track == other.track &&
        artist == other.artist &&
        album == other.album &&
        albumArtist == other.albumArtist &&
        duration == other.duration
}
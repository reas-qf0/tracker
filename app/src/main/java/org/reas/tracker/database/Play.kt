package org.reas.tracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.min
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(tableName = "plays")
data class Play(
    val track: String,
    val artist: String,
    val album: String?,
    val albumArtist: String,
    val playerId: String,
    val timestamp: Long,
    val duration: Long,
    var timePlayed: Long,
    var lastPosition: Long,
    var lastTimestamp: Long,
    var lastPlaying: Boolean,
    val associatedEvents: MutableList<String>
) {
    @OptIn(ExperimentalUuidApi::class)
    @PrimaryKey var id = Uuid.random().toHexDashString()

    val currentPosition
        get() = lastPosition + timestamp - lastTimestamp
    val endTimestamp
        get() = lastTimestamp + duration - lastPosition

    val isNowPlaying
        get() = lastPlaying && currentPosition <= duration
    val isTiny
        get() = timePlayed < EventProcessor.SKIP_MIN_DURATION
    val isSkip
        get() = timePlayed < min(duration / 2, 240L * 1000)
    val isFull
        get() = !isNowPlaying && !isTiny && !isSkip

    companion object {
        fun fromEvent(event: Event): Play = Play(
            track = event.track,
            artist = event.artist,
            album = event.album,
            albumArtist = event.albumArtist,
            timestamp = event.timestamp,
            duration = event.duration,
            timePlayed = 0L,
            lastTimestamp = event.timestamp,
            lastPosition = event.position,
            lastPlaying = event.isPlaying,
            associatedEvents = mutableListOf(event.id),
            playerId = event.playerId
        )
    }
}
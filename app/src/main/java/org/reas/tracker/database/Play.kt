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
    var state: Int,
    val associatedEvents: MutableList<String>
) {
    @OptIn(ExperimentalUuidApi::class)
    @PrimaryKey var id = Uuid.random().toHexDashString()

    fun updateState(lastEventPlaying: Boolean) {
        if (lastEventPlaying) {
            state = PLAYING
            return
        }
        if (timePlayed < EventProcessor.SKIP_MIN_DURATION) {
            state = TINY
            return
        }
        if (timePlayed < min(duration / 2, 240L * 1000)) {
            state = SKIP
            return
        }
        state = FULL
    }

    val currentPosition
        get() = lastPosition + timestamp - lastTimestamp
    val endTimestamp
        get() = lastTimestamp + duration - lastPosition

    val isNowPlaying
        get() = state == PLAYING
    val isTiny
        get() = state == TINY
    val isSkip
        get() = state == SKIP
    val isFull
        get() = state == FULL

    companion object {
        const val PLAYING = 0
        const val TINY = 1
        const val SKIP = 2
        const val FULL = 3

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
            state = PLAYING,
            associatedEvents = mutableListOf(event.id),
            playerId = event.playerId
        )
    }
}
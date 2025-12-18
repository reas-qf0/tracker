package org.reas.tracker.database

import androidx.room.Entity
import androidx.room.Ignore
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
    var state: Int,
    val associatedEvents: MutableList<String>
) {
    @OptIn(ExperimentalUuidApi::class)
    @PrimaryKey var id = Uuid.random().toHexDashString()

    @Ignore
    private var lastPosition = 0L
    @Ignore
    private var lastTimestamp = 0L

    fun updateState(response: List<Event>) {
        val events = response.sortedBy { it.timestamp }
        lastPosition = events.last().position
        lastTimestamp = events.last().timestamp

        timePlayed = 0
        var i = 0
        while (i < events.size - 1) {
            if (events[i].isPlaying)
                timePlayed += events[i + 1].timestamp - events[i].timestamp
            i++
        }

        if (events.last().isPlaying) {
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
            state = PLAYING,
            associatedEvents = mutableListOf(event.id),
            playerId = event.playerId
        )
    }
}
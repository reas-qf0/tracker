package org.reas.tracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.min

@Entity(tableName = "plays")
data class Play(
    val track: String,
    val artist: String,
    val album: String?,
    val albumArtist: String?,
    val timestamp: Long,
    val duration: Long,
    var timePlayed: Long,
    var state: Int,
    val associatedEvents: MutableList<Long>
) {
    @PrimaryKey(autoGenerate = true) var id: Long = 0

    val isNowPlaying
        get() = state == PLAYING
    val isTiny
        get() = state == TINY
    val isSkip
        get() = state == SKIP
    val isFull
        get() = state == FULL

    fun updateState(isNowPlaying: Boolean) {
        if (isNowPlaying) {
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

    companion object {
        const val PLAYING = 0
        const val TINY = 1
        const val SKIP = 2
        const val FULL = 3
    }
}
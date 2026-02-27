package com.reas.tracker2.database.objects

import androidx.room.Entity
import com.reas.tracker2.util.EventProcessor
import kotlin.math.min

@Entity(tableName = "plays", primaryKeys = ["sourceDevice", "sourceApp", "timestamp"])
data class Play(
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
    val associatedEvents: MutableList<Long>
) {
    val lastTimestamp
        get() = associatedEvents.last()

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

    val key
        get() = "$sourceDevice/$sourceApp/$timestamp"
}
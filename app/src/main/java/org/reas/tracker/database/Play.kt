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
    val associatedEvents: MutableList<Long>
) {
    @PrimaryKey(autoGenerate = true) var id: Long = 0

    val isCounted: Boolean
        get() = timePlayed >= min(4 * 60 * 1000, duration / 2)
    val isSkip: Boolean
        get() = !isCounted && timePlayed >= EventProcessor.SKIP_MIN_DURATION
    val isUncountable: Boolean
        get() = !isCounted && !isSkip
}
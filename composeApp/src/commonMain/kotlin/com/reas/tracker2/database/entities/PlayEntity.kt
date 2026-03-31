package com.reas.tracker2.database.entities

import androidx.room.Entity
import androidx.room.Index
import com.reas.tracker2.shared.Play
import kotlin.time.Duration
import kotlin.time.Instant

@Entity(
    tableName = "plays",
    primaryKeys = ["sourceDevice", "sourceApp", "timestamp"],
    indices = [
        Index("trackId", unique = false),
        Index("timestamp", orders = [Index.Order.DESC], unique = false)
    ]
)
data class PlayEntity(
    val trackId: Long,
    val timestamp: Instant,
    val duration: Duration,
    var timePlayed: Duration,
    var lastPosition: Duration,
    var lastPlaying: Boolean,
    val sourceDevice: String,
    val sourceApp: String,
    val associatedEvents: MutableList<Play.EventInfo?>
)
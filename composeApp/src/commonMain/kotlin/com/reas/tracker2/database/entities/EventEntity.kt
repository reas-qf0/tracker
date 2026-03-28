package com.reas.tracker2.database.entities

import androidx.room.Entity
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Instant

@Serializable
@Entity(tableName = "events", primaryKeys = ["sourceApp", "timestamp"])
data class EventEntity(
    val trackId: Long,
    val timestamp: Instant,
    val position: Duration,
    val duration: Duration,
    val isPlaying: Boolean,
    val sourceApp: String
)
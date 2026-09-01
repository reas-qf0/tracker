package com.reas.tracker2.database.entities

import androidx.room3.Entity
import com.reas.tracker2.shared.EventState
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
    val state: EventState,
    val sourceApp: String
)
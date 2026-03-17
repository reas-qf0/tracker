package com.reas.tracker2.shared

import kotlin.time.Instant

interface EventProcessorAdapter {
    suspend fun getLastPlayFromSource(source: Source): Play?
    suspend fun clearQueue(source: Source, timestamp: Instant)
    suspend fun getNextId(user: String): Long?
}
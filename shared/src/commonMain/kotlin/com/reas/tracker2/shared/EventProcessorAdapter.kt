package com.reas.tracker2.shared

import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface EventProcessorAdapter {
    fun getEvents(): Flow<List<Event>>
    suspend fun insertPlay(play: Play): Long
    suspend fun getLastPlayFromSource(source: Source): Play?
    suspend fun clearQueue(source: Source, timestamp: Instant)
}
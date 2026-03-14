package com.reas.tracker2.database

import com.reas.tracker2.shared.EventProcessorAdapter
import com.reas.tracker2.shared.Source
import kotlin.time.Instant

class EventProcessorAdapterImpl(
    private val repository: Repository
): EventProcessorAdapter {
    override suspend fun getLastPlayFromSource(source: Source) = repository.getLastPlayFromSource(source)

    override suspend fun clearQueue(source: Source, timestamp: Instant) = repository.clearQueue(source.app, timestamp)
}
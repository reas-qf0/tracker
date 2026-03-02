package com.reas.tracker2.database

import com.reas.tracker2.shared.EventProcessorAdapter
import com.reas.tracker2.shared.Play
import com.reas.tracker2.shared.Source
import kotlin.time.Instant

class EventProcessorAdapterImpl(
    private val repository: Repository
): EventProcessorAdapter {
    override fun getEvents() = repository.getEvents()

    override suspend fun insertPlay(play: Play) = repository.insertPlay(play)

    override suspend fun getLastPlayFromSource(source: Source) = repository.getLastPlayFromSource(source)

    override suspend fun clearQueue(app: String, timestamp: Instant) = repository.clearQueue(app, timestamp)
}
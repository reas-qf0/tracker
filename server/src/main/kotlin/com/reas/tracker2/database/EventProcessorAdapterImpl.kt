package com.reas.tracker2.database

import com.reas.tracker2.shared.Event
import com.reas.tracker2.shared.EventProcessorAdapter
import com.reas.tracker2.shared.Play
import com.reas.tracker2.shared.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

class EventProcessorAdapterImpl(
    private val repository: Repository
) : EventProcessorAdapter {
    private val eventFlow = MutableSharedFlow<Event>()
    private val playFlow = MutableSharedFlow<Play>()

    suspend fun addEvent(event: Event) {
        eventFlow.emit(event)
    }
    suspend fun onPlay(process: suspend (Play) -> Unit) {
        playFlow.collect(process)
    }

    override fun getEvents(): Flow<List<Event>> =
        eventFlow.asSharedFlow().map { event -> listOf(event) }

    override suspend fun insertPlay(play: Play): Long {
        repository.insertPlay(play)
        playFlow.emit(play)
        return 0L
    }

    override suspend fun getLastPlayFromSource(source: Source): Play? =
        repository.getLastPlayFromSource(source)

    override suspend fun clearQueue(source: Source, timestamp: Instant) {}
}
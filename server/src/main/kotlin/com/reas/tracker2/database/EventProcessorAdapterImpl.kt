package com.reas.tracker2.database

import com.reas.tracker2.shared.EventProcessorAdapter
import com.reas.tracker2.shared.Play
import com.reas.tracker2.shared.Source
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Instant

class EventProcessorAdapterImpl(
    private val repository: Repository
) : EventProcessorAdapter {
    private val ids = repository.getNextIds()
    private val idLock = Mutex()

    override suspend fun getLastPlayFromSource(source: Source): Play? =
        repository.getLastPlayFromSource(source)

    override suspend fun clearQueue(source: Source, timestamp: Instant) {}

    override suspend fun getNextId(user: String) =
        idLock.withLock {
            val x = (ids[user] ?: 0) + 1
            ids[user] = x
            x
        }
}
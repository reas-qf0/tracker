package com.reas.tracker2.ui.viewmodels

import com.reas.tracker2.database.Repository
import com.reas.tracker2.network.TrackerInstanceClient
import com.reas.tracker2.settings.*
import com.reas.tracker2.util.InMemoryLog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.runningFold

class DebugScreenViewModel(
    private val repository: Repository,
    private val settings: Settings,
    private val client: TrackerInstanceClient,
    private val inMemoryLog: InMemoryLog
) : TrackerViewModel() {
    val eventCount
        get() = repository.getEventCount().asIntStateFlow()
    val unsyncedEventCount
        get() = repository.getUnsyncedEventCount().asIntStateFlow()
    val playCount
        get() = repository.getPlayCount().asIntStateFlow()

    val mediaEventLog
        get() = inMemoryLog["MediaEvents"]
            .runningFold("") { a, b -> "$a$b\n" }
            .asStringStateFlow()

    suspend fun refreshApiKey() {
        repository.deleteKey(
            settings[instanceHostName],
            settings[instancePort],
            settings[username]
        )
        client.tryLogin()
    }

    suspend fun flushAllEvents() {
        repository.getEvents().first().forEach { event ->
            repository.insertEventInSync(event)
        }
        client.submitEvents()
    }
}
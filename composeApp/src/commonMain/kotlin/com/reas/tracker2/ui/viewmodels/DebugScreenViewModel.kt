package com.reas.tracker2.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reas.tracker2.database.Repository
import com.reas.tracker2.network.TrackerInstanceClient
import com.reas.tracker2.settings.*
import com.reas.tracker2.util.InMemoryLog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn

class DebugScreenViewModel(
    private val repository: Repository,
    private val settings: Settings,
    private val client: TrackerInstanceClient,
    private val inMemoryLog: InMemoryLog
) : ViewModel() {
    val eventCount
        get() = repository.getEventCount().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            0)
    val unprocessedEventCount
        get() = repository.getUnprocessedEventCount().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            0)
    val unsyncedEventCount
        get() = repository.getUnsyncedEventCount().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            0)
    val playCount
        get() = repository.getPlayCount().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            0)

    val mediaEventLog
        get() = inMemoryLog["MediaEvents"]
            .runningFold("") { a, b -> "$a$b\n" }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                "")

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

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
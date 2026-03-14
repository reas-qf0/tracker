package com.reas.tracker2.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reas.tracker2.database.Repository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class DebugScreenViewModel(private val repository: Repository) : ViewModel() {
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

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
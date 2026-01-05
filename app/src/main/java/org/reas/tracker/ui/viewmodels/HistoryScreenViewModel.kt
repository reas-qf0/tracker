package org.reas.tracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.reas.tracker.database.Repository

class HistoryScreenViewModel(private val repository: Repository): ViewModel() {
    val nowPlaying = repository.getNowPlayingTracks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf()
        )
    val history = repository.getRecentPlays(50)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf()
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
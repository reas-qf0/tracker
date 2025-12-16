package org.reas.tracker.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.reas.tracker.database.Play
import org.reas.tracker.database.Repository

data class HistoryUiState(val history: List<Play> = listOf())

class HistoryScreenViewModel(private val repository: Repository): ViewModel() {
    private fun timePlayedToString(time: Long): String {
        var minutesTotal = time / 1000 / 60
        if (time % (1000 * 60) > 1000 * 30)
            minutesTotal++

        val minutes = minutesTotal % 60
        val hours = (minutesTotal / 60) % 24
        val days = (minutesTotal / 60 / 24)
        return "${days}d ${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
    }
    val history = repository.getRecentPlays(50)
        .map { HistoryUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = HistoryUiState()
        )

    fun artistPlays(artist: String) = repository.getArtistPlays(artist)
        .map { it.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun artistTimePlayed(artist: String) = repository.getArtistTimePlayed(artist)
        .map { timePlayedToString(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun trackPlays(artist: String, track: String) = repository.getTrackPlays(artist, track)
        .map { it.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun trackTimePlayed(artist: String, track: String) = repository.getTrackTimePlayed(artist, track)
        .map { timePlayedToString(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun albumPlays(artist: String, album: String) = repository.getAlbumPlays(artist, album)
        .map { it.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun albumTimePlayed(artist: String, album: String) = repository.getAlbumTimePlayed(artist, album)
        .map { timePlayedToString(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
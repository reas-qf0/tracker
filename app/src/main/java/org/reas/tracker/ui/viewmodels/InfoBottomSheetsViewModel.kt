package org.reas.tracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.reas.tracker.database.Repository
import org.reas.tracker.util.DateTimeFormatter.timeMsToString

class InfoBottomSheetsViewModel(private val repository: Repository) : ViewModel() {
    fun artistPlays(artist: String) = repository.getArtistPlays(artist)
        .map { it.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun artistTimePlayed(artist: String) = repository.getArtistTimePlayed(artist)
        .map { timeMsToString(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun trackPlays(artist: String, track: String, album: String?) = repository.getTrackPlays(artist, track, album)
        .map { it.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun trackTimePlayed(artist: String, track: String, album: String?) = repository.getTrackTimePlayed(artist, track, album)
        .map { timeMsToString(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun albumPlays(artist: String, album: String) = repository.getAlbumPlays(artist, album)
        .map { it.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun albumTimePlayed(artist: String, album: String) = repository.getAlbumTimePlayed(artist, album)
        .map { timeMsToString(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
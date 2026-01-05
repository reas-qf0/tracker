package org.reas.tracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.reas.tracker.database.Repository

class ChartsScreenViewModel(private val repository: Repository) : ViewModel() {
    fun artists(start: Long, end: Long) = repository.getMostPlayedArtists(start, end, 50)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf()
        )

    fun albums(start: Long, end: Long) = repository.getMostPlayedAlbums(start, end, 50)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf()
        )

    fun tracks(start: Long, end: Long) = repository.getMostPlayedTracks(start, end, 50)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf()
        )

    fun artistsByPlayCount(start: Long, end: Long) = repository.getMostPlayedArtistsByPlayCount(start, end, 50)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf()
        )

    fun albumsByPlayCount(start: Long, end: Long) = repository.getMostPlayedAlbumsByPlayCount(start, end, 50)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf()
        )

    fun tracksByPlayCount(start: Long, end: Long) = repository.getMostPlayedTracksByPlayCount(start, end, 50)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf()
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
package com.reas.tracker2.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reas.tracker2.database.Repository
import com.reas.tracker2.shared.Album
import com.reas.tracker2.shared.Artist
import com.reas.tracker2.shared.TimePeriod
import com.reas.tracker2.shared.TrackWithAlbum
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.minutes

class InfoBottomSheetsViewModel(private val repository: Repository) : ViewModel() {
    fun artistPlays(artist: Artist) = repository.getArtistPlays(artist, TimePeriod.ALLTIME)
        .map { it.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun artistTimePlayed(artist: Artist) = repository.getArtistTimePlayed(artist, TimePeriod.ALLTIME)
        .map { it.inWholeMinutes.minutes.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun trackPlays(track: TrackWithAlbum) = repository.getTrackPlays(track, TimePeriod.ALLTIME)
        .map { it.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun trackTimePlayed(track: TrackWithAlbum) = repository.getTrackTimePlayed(track, TimePeriod.ALLTIME)
        .map { it.inWholeMinutes.minutes.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun albumPlays(album: Album) = repository.getAlbumPlays(album, TimePeriod.ALLTIME)
        .map { it.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun albumTimePlayed(album: Album) = repository.getAlbumTimePlayed(album, TimePeriod.ALLTIME)
        .map { it.inWholeMinutes.minutes.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
package com.reas.tracker2.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.reas.tracker2.database.Repository

class TrackInfoScreenViewModel(private val repository: Repository): ViewModel() {
    fun plays(artist: String, track: String, album: String?, start: Long, end: Long) = repository.getTrackPlays(artist, track, album, start, end)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = -1
        )

    fun timePlayed(artist: String, track: String, album: String?, start: Long, end: Long) = repository.getTrackTimePlayed(artist, track, album, start, end)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = -1L
        )

    fun rank(time: Long, start: Long, end: Long) = repository.getTrackRank(time, start, end)
        .map { "#" + (it + 1).toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun playRank(count: Int, start: Long, end: Long) = repository.getTrackRankByPlayCount(count, start, end)
        .map { "#" + (it + 1).toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
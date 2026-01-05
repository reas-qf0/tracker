package org.reas.tracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.reas.tracker.database.Repository

class TrackHistoryViewModel(private val repository: Repository): ViewModel() {
    fun history(artist: String, track: String, album: String?) = Pager(
        initialKey = 0,
        pagingSourceFactory = { repository.getTrackHistory(artist, track, album) },
        config = PagingConfig(pageSize = 50, initialLoadSize = 50)
    ).flow.cachedIn(viewModelScope)

    fun trackPlays(artist: String, track: String, album: String?) = repository.getTrackPlays(artist, track, album)
        .map { it.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
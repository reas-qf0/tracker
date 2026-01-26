package com.reas.tracker2.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.reas.tracker2.database.Repository
import com.reas.tracker2.network.NetworkRepository

class TrackHistoryViewModel(
    private val repository: Repository,
    private val networkRepository: NetworkRepository
): ViewModel() {
    fun history(artist: String, track: String, album: String?) = Pager(
        initialKey = 0,
        pagingSourceFactory = { repository.getTrackHistory(artist, track, album) },
        config = PagingConfig(pageSize = 50, initialLoadSize = 50)
    ).flow.cachedIn(viewModelScope)

    fun trackPlays(artist: String, track: String, album: String?) = repository.getTrackPlays(artist, track, album, 0L, Long.MAX_VALUE)
        .map { it.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    suspend fun getAlbumImageUrl(artist: String, album: String) =
        networkRepository.getAlbumImageUrl(artist, album, "large")

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
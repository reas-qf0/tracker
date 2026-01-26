package com.reas.tracker2.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import com.reas.tracker2.database.Play
import com.reas.tracker2.database.Repository
import com.reas.tracker2.network.NetworkRepository

class HistoryScreenViewModel(
    private val repository: Repository,
    private val networkRepository: NetworkRepository
): ViewModel() {
    val nowPlaying = repository.getNowPlayingTracks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf()
        )
    val history = Pager(
        initialKey = 0,
        pagingSourceFactory = { repository.getRecentPlays() },
        config = PagingConfig(pageSize = 50, initialLoadSize = 50)
    ).flow.cachedIn(viewModelScope)

    suspend fun getImageUrl(scrobble: Play): String? {
        if (scrobble.album != null) {
            return networkRepository.getAlbumImageUrl(scrobble.albumArtist, scrobble.album, "large")
        }
        return null
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
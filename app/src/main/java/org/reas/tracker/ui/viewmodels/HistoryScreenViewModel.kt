package org.reas.tracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.reas.tracker.database.Play
import org.reas.tracker.database.Repository
import org.reas.tracker.network.NetworkRepository
import org.reas.tracker.supabase.CustomImageStorage

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
        repository.getCustomImage(
            listOf("track", scrobble.artist, scrobble.track)
        )?.let {
            return CustomImageStorage.get(it)
        }
        if (scrobble.album != null) {
            repository.getCustomImage(
                listOf("album", scrobble.albumArtist, scrobble.album)
            )?.let {
                return CustomImageStorage.get(it)
            }
            return networkRepository.getAlbumImageUrl(scrobble.albumArtist, scrobble.album, "large")
        }
        return null
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
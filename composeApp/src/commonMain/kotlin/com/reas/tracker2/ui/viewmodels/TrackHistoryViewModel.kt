package com.reas.tracker2.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import com.reas.tracker2.database.Repository
import com.reas.tracker2.network.NetworkRepository
import com.reas.tracker2.shared.Play
import com.reas.tracker2.shared.TimePeriod
import com.reas.tracker2.shared.TrackWithAlbum
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TrackHistoryViewModel(
    private val repository: Repository,
    private val networkRepository: NetworkRepository
): ViewModel() {
    fun history(track: TrackWithAlbum) = Pager(
        initialKey = 0,
        pagingSourceFactory = { repository.getTrackHistory(track) },
        config = PagingConfig(pageSize = 50, initialLoadSize = 50)
    ).flow.cachedIn(viewModelScope)
        .map { it.map { entity -> repository.playEntityToObject(entity) } }

    fun trackPlays(track: TrackWithAlbum) = repository.getTrackPlays(track, TimePeriod.ALLTIME)
        .map { it.toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    suspend fun getImageUrl(scrobble: Play): String? {
        scrobble.asAlbumOrNull?.let { album ->
            return networkRepository.getAlbumImageUrl(album, "large")
        }
        return null
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
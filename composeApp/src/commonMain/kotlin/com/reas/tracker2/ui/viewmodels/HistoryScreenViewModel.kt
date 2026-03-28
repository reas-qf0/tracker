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
import kotlinx.coroutines.flow.map

class HistoryScreenViewModel(
    private val repository: Repository,
    private val networkRepository: NetworkRepository
): ViewModel() {
    val history = Pager(
        initialKey = 0,
        pagingSourceFactory = { repository.getRecentPlays() },
        config = PagingConfig(pageSize = 50, initialLoadSize = 50)
    ).flow.cachedIn(viewModelScope)
        .map { it.map { entity -> repository.playEntityToObject(entity) } }

    suspend fun getImageUrl(scrobble: Play): String? {
        scrobble.asAlbumOrNull?.let { album ->
            return networkRepository.getAlbumImageUrl(album, "large")
        }
        return null
    }
}
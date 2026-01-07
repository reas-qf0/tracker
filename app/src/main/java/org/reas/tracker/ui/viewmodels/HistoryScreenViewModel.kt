package org.reas.tracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.reas.tracker.database.Repository

class HistoryScreenViewModel(private val repository: Repository): ViewModel() {
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

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
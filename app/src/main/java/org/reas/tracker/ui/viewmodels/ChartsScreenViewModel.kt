package org.reas.tracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import org.reas.tracker.database.Repository

class ChartsScreenViewModel(private val repository: Repository) : ViewModel() {
    fun artists(start: Long, end: Long) = Pager(
        initialKey = 0,
        pagingSourceFactory = { repository.getMostPlayedArtists(start, end) },
        config = PagingConfig(pageSize = 50, initialLoadSize = 50)
    ).flow.cachedIn(viewModelScope)

    fun albums(start: Long, end: Long) = Pager(
        initialKey = 0,
        pagingSourceFactory = { repository.getMostPlayedAlbums(start, end) },
        config = PagingConfig(pageSize = 50, initialLoadSize = 50)
    ).flow.cachedIn(viewModelScope)

    fun tracks(start: Long, end: Long) = Pager(
        initialKey = 0,
        pagingSourceFactory = { repository.getMostPlayedTracks(start, end) },
        config = PagingConfig(pageSize = 50, initialLoadSize = 50)
    ).flow.cachedIn(viewModelScope)

    fun artistsByPlayCount(start: Long, end: Long) = Pager(
        initialKey = 0,
        pagingSourceFactory = { repository.getMostPlayedArtistsByPlayCount(start, end) },
        config = PagingConfig(pageSize = 50, initialLoadSize = 50)
    ).flow.cachedIn(viewModelScope)

    fun albumsByPlayCount(start: Long, end: Long) = Pager(
        initialKey = 0,
        pagingSourceFactory = { repository.getMostPlayedAlbumsByPlayCount(start, end) },
        config = PagingConfig(pageSize = 50, initialLoadSize = 50)
    ).flow.cachedIn(viewModelScope)

    fun tracksByPlayCount(start: Long, end: Long) = Pager(
        initialKey = 0,
        pagingSourceFactory = { repository.getMostPlayedTracksByPlayCount(start, end) },
        config = PagingConfig(pageSize = 50, initialLoadSize = 50)
    ).flow.cachedIn(viewModelScope)
}
package org.reas.tracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.reas.tracker.database.Repository
import org.reas.tracker.ui.components.ChartEntryUiState
import org.reas.tracker.ui.navigation.BottomSheetInfo
import org.reas.tracker.util.DateTimeFormatter.timeMsToString

class ArtistInfoScreenViewModel(private val repository: Repository): ViewModel() {
    private fun<T : Any> get(factory: () -> PagingSource<Int, T>, transform: (T) -> ChartEntryUiState) = Pager(
        initialKey = 0,
        pagingSourceFactory = factory,
        config = PagingConfig(pageSize = 50, initialLoadSize = 50)
    ).flow
        .cachedIn(viewModelScope)
        .map {
            it.map { info -> transform(info) }
        }

    fun artistPlays(artist: String, start: Long, end: Long) = repository.getArtistPlays(artist, start, end)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = 0
        )

    fun artistTimePlayed(artist: String, start: Long, end: Long) = repository.getArtistTimePlayed(artist, start, end)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = 0L
        )

    fun rank(time: Long, start: Long, end: Long) = repository.getArtistRank(time, start, end)
        .map { "#" + (it + 1).toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun playRank(count: Int, start: Long, end: Long) = repository.getArtistRankByPlayCount(count, start, end)
        .map { "#" + (it + 1).toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun topAlbums(artist: String, start: Long, end: Long) = get(
        { repository.getMostPlayedAlbumsFromArtist(artist, start, end) }
    ) { info ->
        ChartEntryUiState(
            label = info.album,
            label2 = null,
            metric = info.metric.toDouble(),
            metricAsString = timeMsToString(info.metric),
            bottomSheetInfo = BottomSheetInfo(artist = info.artist, albumArtist = info.artist, album = info.album)
        )
    }

    fun topAlbumsByPlayCount(artist: String, start: Long, end: Long) = get(
        { repository.getMostPlayedAlbumsFromArtistByPlayCount(artist, start, end) }
    ) { info ->
        ChartEntryUiState(
            label = info.album,
            label2 = null,
            metric = info.metric.toDouble(),
            metricAsString = "${info.metric} plays",
            bottomSheetInfo = BottomSheetInfo(artist = info.artist, albumArtist = info.artist, album = info.album)
        )
    }

    fun topTracks(artist: String, start: Long, end: Long) = get(
        { repository.getMostPlayedTracksFromArtist(artist, start, end) }
    ) { info ->
        ChartEntryUiState(
            label = info.track,
            label2 = null,
            metric = info.metric.toDouble(),
            metricAsString = timeMsToString(info.metric),
            bottomSheetInfo = BottomSheetInfo(artist = info.artist, track = info.track)
        )
    }

    fun topTracksByPlayCount(artist: String,start: Long, end: Long) = get(
        { repository.getMostPlayedTracksFromArtistByPlayCount(artist, start, end) }
    ) { info ->
        ChartEntryUiState(
            label = info.track,
            label2 = null,
            metric = info.metric.toDouble(),
            metricAsString = "${info.metric} plays",
            bottomSheetInfo = BottomSheetInfo(artist = info.artist, track = info.track)
        )
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
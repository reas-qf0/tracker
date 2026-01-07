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

class AlbumInfoScreenViewModel(private val repository: Repository): ViewModel() {
    private fun<T : Any> get(factory: () -> PagingSource<Int, T>, transform: (T) -> ChartEntryUiState) = Pager(
        initialKey = 0,
        pagingSourceFactory = factory,
        config = PagingConfig(pageSize = 50, initialLoadSize = 50)
    ).flow
        .cachedIn(viewModelScope)
        .map {
            it.map { info -> transform(info) }
        }

    fun plays(artist: String, album: String, start: Long, end: Long) = repository.getAlbumPlays(artist, album, start, end)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = -1
        )

    fun timePlayed(artist: String, album: String, start: Long, end: Long) = repository.getAlbumTimePlayed(artist, album, start, end)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = -1L
        )

    fun rank(time: Long, start: Long, end: Long) = repository.getAlbumRank(time, start, end)
        .map { "#" + (it + 1).toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun playRank(count: Int, start: Long, end: Long) = repository.getAlbumRankByPlayCount(count, start, end)
        .map { "#" + (it + 1).toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun topTracks(artist: String, album: String, start: Long, end: Long) = get(
        { repository.getMostPlayedTracksFromAlbum(artist, album, start, end) }
    ) { info ->
        ChartEntryUiState(
            label = info.track,
            label2 = null,
            metric = info.metric.toDouble(),
            metricAsString = timeMsToString(info.metric),
            bottomSheetInfo = BottomSheetInfo(artist = info.artist, track = info.track)
        )
    }

    fun topTracksByPlayCount(artist: String, album: String, start: Long, end: Long) = get(
        { repository.getMostPlayedTracksFromAlbumByPlayCount(artist, album, start, end) }
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
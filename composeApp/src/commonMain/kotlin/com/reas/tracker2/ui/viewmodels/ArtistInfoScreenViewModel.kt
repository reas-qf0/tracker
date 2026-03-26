package com.reas.tracker2.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.reas.tracker2.database.Repository
import com.reas.tracker2.settings.*
import com.reas.tracker2.shared.TimePeriod
import com.reas.tracker2.ui.components.ChartEntryUiState
import com.reas.tracker2.ui.navigation.BottomSheetInfo
import com.reas.tracker2.ui.navigation.ChartSort
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.time.inMs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class ArtistInfoScreenViewModel(
    private val repository: Repository,
    private val settings: Settings,
): ViewModel() {
    private fun<T : Any> get(factory: () -> PagingSource<Int, T>, transform: (T) -> ChartEntryUiState) = Pager(
        initialKey = 0,
        pagingSourceFactory = factory,
        config = PagingConfig(pageSize = 50, initialLoadSize = 50)
    ).flow
        .cachedIn(viewModelScope)
        .map {
            it.map { info -> transform(info) }
        }

    fun plays(artist: String, period: TimePeriod) = repository.getArtistPlays(artist, period)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = -1
        )

    fun timePlayed(artist: String, period: TimePeriod) = repository.getArtistTimePlayed(artist, period)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = -Duration.INFINITE
        )

    fun rank(artist: String, period: TimePeriod) = repository.getArtistRank(artist, period)
        .map { "#" + (it + 1).toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun playRank(artist: String, period: TimePeriod) = repository.getArtistRankByPlayCount(artist, period)
        .map { "#" + (it + 1).toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun topAlbums(artist: String, period: TimePeriod) = get(
        { repository.getMostPlayedAlbumsFromArtist(artist, period) }
    ) { info ->
        ChartEntryUiState(
            label = info._album,
            label2 = null,
            key = info.toString(),
            metric = info.timePlayed.inMs,
            metricAsString = info.timePlayed.inWholeMinutes.minutes.toString(),
            bottomSheetInfo = BottomSheetInfo(album = info.album)
        )
    }

    fun topAlbumsByPlayCount(artist: String, period: TimePeriod) = get(
        { repository.getMostPlayedAlbumsFromArtistByPlayCount(artist, period) }
    ) { info ->
        ChartEntryUiState(
            label = info._album,
            label2 = null,
            key = info.toString(),
            metric = info.playCount.toDouble(),
            metricAsString = "${info.playCount} plays",
            bottomSheetInfo = BottomSheetInfo(album = info.album)
        )
    }

    fun topTracks(artist: String, period: TimePeriod) = get(
        { repository.getMostPlayedTracksFromArtist(artist, period) }
    ) { info ->
        ChartEntryUiState(
            label = info._track,
            label2 = null,
            key = info.toString(),
            metric = info.timePlayed.inMs,
            metricAsString = info.timePlayed.inWholeMinutes.minutes.toString(),
            bottomSheetInfo = BottomSheetInfo(track = info.track)
        )
    }

    fun topTracksByPlayCount(artist: String, period: TimePeriod) = get(
        { repository.getMostPlayedTracksFromArtistByPlayCount(artist, period) }
    ) { info ->
        ChartEntryUiState(
            label = info._track,
            label2 = null,
            key = info.toString(),
            metric = info.playCount.toDouble(),
            metricAsString = "${info.playCount} plays",
            bottomSheetInfo = BottomSheetInfo(track = info.track)
        )
    }

    fun sort() = settings.flow(chartSort).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = settings[chartSort]
    )
    suspend fun setSort(sort: ChartSort) {
        settings[chartSort] = sort
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
package com.reas.tracker2.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.reas.tracker2.database.Repository
import com.reas.tracker2.network.NetworkRepository
import com.reas.tracker2.shared.Album
import com.reas.tracker2.shared.TimePeriod
import com.reas.tracker2.ui.components.ChartEntryUiState
import com.reas.tracker2.ui.navigation.BottomSheetInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.time.inMs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class AlbumInfoScreenViewModel(
    private val repository: Repository,
    private val networkRepository: NetworkRepository
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

    fun plays(album: Album, period: TimePeriod) = repository.getAlbumPlays(album, period)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = -1
        )

    fun timePlayed(album: Album, period: TimePeriod) = repository.getAlbumTimePlayed(album, period)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = -Duration.INFINITE
        )

    fun rank(time: Duration, period: TimePeriod) = repository.getAlbumRank(time, period)
        .map { "#" + (it + 1).toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun playRank(count: Int, period: TimePeriod) = repository.getAlbumRankByPlayCount(count, period)
        .map { "#" + (it + 1).toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun topTracks(album: Album, period: TimePeriod) = get(
        { repository.getMostPlayedTracksFromAlbum(album, period) }
    ) { info ->
        ChartEntryUiState(
            label = info._track,
            label2 = null,
            metric = info.timePlayed.inMs,
            metricAsString = info.timePlayed.inWholeMinutes.minutes.toString(),
            bottomSheetInfo = BottomSheetInfo(track = info.track.withAlbum())
        )
    }

    fun topTracksByPlayCount(album: Album, period: TimePeriod) = get(
        { repository.getMostPlayedTracksFromAlbumByPlayCount(album, period) }
    ) { info ->
        ChartEntryUiState(
            label = info._track,
            label2 = null,
            metric = info.playCount.toDouble(),
            metricAsString = "${info.playCount} plays",
            bottomSheetInfo = BottomSheetInfo(track = info.track.withAlbum())
        )
    }

    suspend fun getAlbumImageUrl(album: Album) =
        networkRepository.getAlbumImageUrl(album, "large")

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
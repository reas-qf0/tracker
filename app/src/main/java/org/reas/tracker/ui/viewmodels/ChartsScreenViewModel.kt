package org.reas.tracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.reas.tracker.database.Repository
import org.reas.tracker.network.NetworkRepository
import org.reas.tracker.ui.components.ChartEntryUiState
import org.reas.tracker.ui.navigation.BottomSheetInfo
import org.reas.tracker.ui.navigation.ChartSort
import org.reas.tracker.ui.navigation.ChartType
import org.reas.tracker.ui.navigation.Charts
import org.reas.tracker.util.DateTimeFormatter.timeMsToString

class ChartsScreenViewModel(
    private val repository: Repository,
    private val networkRepository: NetworkRepository
) : ViewModel() {
    private fun<T : Any> get(factory: () -> PagingSource<Int, T>, transform: (T) -> ChartEntryUiState) = Pager(
        initialKey = 0,
        pagingSourceFactory = factory,
        config = PagingConfig(pageSize = 50, initialLoadSize = 50)
    ).flow
        .cachedIn(viewModelScope)
        .map {
            it.map { info -> transform(info) }
        }

    private fun artists(start: Long, end: Long) = get(
        { repository.getMostPlayedArtists(start, end) }
    ) { info ->
        ChartEntryUiState(
            label = info.artist,
            label2 = null,
            metric = info.metric.toDouble(),
            metricAsString = timeMsToString(info.metric),
            bottomSheetInfo = BottomSheetInfo(artist = info.artist)
        )
    }

    private fun albums(start: Long, end: Long) = get(
        { repository.getMostPlayedAlbums(start, end) }
    ) { info ->
        ChartEntryUiState(
            label = info.album,
            label2 = info.artist,
            metric = info.metric.toDouble(),
            metricAsString = timeMsToString(info.metric),
            bottomSheetInfo = BottomSheetInfo(artist = info.artist, albumArtist = info.artist, album = info.album),
            url = { getAlbumImageUrl(info.artist, info.album) }
        )
    }

    private fun tracks(start: Long, end: Long) = get(
        { repository.getMostPlayedTracks(start, end) }
    ) { info ->
        ChartEntryUiState(
            label = info.track,
            label2 = info.artist,
            metric = info.metric.toDouble(),
            metricAsString = timeMsToString(info.metric),
            bottomSheetInfo = BottomSheetInfo(artist = info.artist, track = info.track)
        )
    }

    private fun artistsByPlayCount(start: Long, end: Long) = get(
        { repository.getMostPlayedArtistsByPlayCount(start, end) }
    ) { info ->
        ChartEntryUiState(
            label = info.artist,
            label2 = null,
            metric = info.metric.toDouble(),
            metricAsString = "${info.metric} plays",
            bottomSheetInfo = BottomSheetInfo(artist = info.artist)
        )
    }

    private fun albumsByPlayCount(start: Long, end: Long) = get(
        { repository.getMostPlayedAlbumsByPlayCount(start, end) }
    ) { info ->
        ChartEntryUiState(
            label = info.album,
            label2 = info.artist,
            metric = info.metric.toDouble(),
            metricAsString = "${info.metric} plays",
            bottomSheetInfo = BottomSheetInfo(artist = info.artist, albumArtist = info.artist, album = info.album),
            url = { getAlbumImageUrl(info.artist, info.album) }
        )
    }

    private fun tracksByPlayCount(start: Long, end: Long) = get(
        { repository.getMostPlayedTracksByPlayCount(start, end) }
    ) { info ->
        ChartEntryUiState(
            label = info.track,
            label2 = info.artist,
            metric = info.metric.toDouble(),
            metricAsString = "${info.metric} plays",
            bottomSheetInfo = BottomSheetInfo(artist = info.artist, track = info.track)
        )
    }

    fun getInfo(arguments: Charts): Flow<PagingData<ChartEntryUiState>> {
        val sort = arguments.sort
        val type = arguments.type
        val start = 0L
        val end = Long.MAX_VALUE

        return when {
            (type == ChartType.ARTISTS && sort == ChartSort.TIME) ->
                artists(start, end)
            (type == ChartType.ALBUMS && sort == ChartSort.TIME) ->
                albums(start, end)
            (type == ChartType.TRACKS && sort == ChartSort.TIME) ->
                tracks(start, end)
            (type == ChartType.ARTISTS && sort == ChartSort.PLAYS) ->
                artistsByPlayCount(start, end)
            (type == ChartType.ALBUMS && sort == ChartSort.PLAYS) ->
                albumsByPlayCount(start, end)
            (type == ChartType.TRACKS && sort == ChartSort.PLAYS) ->
                tracksByPlayCount(start, end)

            else -> throw IllegalArgumentException("unreachable")
        }
    }

    suspend fun getAlbumImageUrl(artist: String, album: String) =
        networkRepository.getAlbumImageUrl(artist, album, "large")
}
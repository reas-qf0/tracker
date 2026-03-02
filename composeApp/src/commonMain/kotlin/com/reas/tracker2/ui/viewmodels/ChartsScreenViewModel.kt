package com.reas.tracker2.ui.viewmodels

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
import com.reas.tracker2.database.Repository
import com.reas.tracker2.network.NetworkRepository
import com.reas.tracker2.shared.Album
import com.reas.tracker2.shared.TimePeriod
import com.reas.tracker2.shared.Track
import com.reas.tracker2.ui.components.ChartEntryUiState
import com.reas.tracker2.ui.navigation.BottomSheetInfo
import com.reas.tracker2.ui.navigation.ChartSort
import com.reas.tracker2.ui.navigation.ChartType
import com.reas.tracker2.ui.navigation.Charts
import com.reas.tracker2.util.DateTimeFormatter.timeMsToString
import org.koin.core.time.inMs
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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

    private fun artists(period: TimePeriod) = get(
        { repository.getMostPlayedArtists(period) }
    ) { info ->
        ChartEntryUiState(
            label = info.artist,
            label2 = null,
            metric = info.timePlayed.inMs,
            metricAsString = info.timePlayed.inWholeMinutes.minutes.toString(),
            bottomSheetInfo = BottomSheetInfo(artist = info.artist),
            url = { getArtistImageUrl(info.artist) }
        )
    }

    private fun albums(period: TimePeriod) = get(
        { repository.getMostPlayedAlbums(period) }
    ) { info ->
        ChartEntryUiState(
            label = info._album,
            label2 = info._artist,
            metric = info.timePlayed.inMs,
            metricAsString = info.timePlayed.inWholeMinutes.minutes.toString(),
            bottomSheetInfo = BottomSheetInfo(album = info.album),
            url = { getAlbumImageUrl(info.album) }
        )
    }

    private fun tracks(period: TimePeriod) = get(
        { repository.getMostPlayedTracks(period) }
    ) { info ->
        ChartEntryUiState(
            label = info._track,
            label2 = info._artist,
            metric = info.timePlayed.inMs,
            metricAsString = info.timePlayed.inWholeMinutes.minutes.toString(),
            bottomSheetInfo = BottomSheetInfo(track = info.track.withAlbum()),
            url = { getTrackImageUrl(info.track) }
        )
    }

    private fun artistsByPlayCount(period: TimePeriod) = get(
        { repository.getMostPlayedArtistsByPlayCount(period) }
    ) { info ->
        ChartEntryUiState(
            label = info.artist,
            label2 = null,
            metric = info.playCount.toDouble(),
            metricAsString = "${info.playCount} plays",
            bottomSheetInfo = BottomSheetInfo(artist = info.artist),
            url = { getArtistImageUrl(info.artist) }
        )
    }

    private fun albumsByPlayCount(period: TimePeriod) = get(
        { repository.getMostPlayedAlbumsByPlayCount(period) }
    ) { info ->
        ChartEntryUiState(
            label = info._album,
            label2 = info._artist,
            metric = info.playCount.toDouble(),
            metricAsString = "${info.playCount} plays",
            bottomSheetInfo = BottomSheetInfo(album = info.album),
            url = { getAlbumImageUrl(info.album) }
        )
    }

    private fun tracksByPlayCount(period: TimePeriod) = get(
        { repository.getMostPlayedTracksByPlayCount(period) }
    ) { info ->
        ChartEntryUiState(
            label = info._track,
            label2 = info._artist,
            metric = info.playCount.toDouble(),
            metricAsString = "${info.playCount} plays",
            bottomSheetInfo = BottomSheetInfo(track = info.track.withAlbum()),
            url = { getTrackImageUrl(info.track) }
        )
    }

    fun getInfo(arguments: Charts): Flow<PagingData<ChartEntryUiState>> {
        val sort = arguments.sort
        val type = arguments.type
        val period = TimePeriod.ALLTIME

        return when {
            (type == ChartType.ARTISTS && sort == ChartSort.TIME) ->
                artists(period)
            (type == ChartType.ALBUMS && sort == ChartSort.TIME) ->
                albums(period)
            (type == ChartType.TRACKS && sort == ChartSort.TIME) ->
                tracks(period)
            (type == ChartType.ARTISTS && sort == ChartSort.PLAYS) ->
                artistsByPlayCount(period)
            (type == ChartType.ALBUMS && sort == ChartSort.PLAYS) ->
                albumsByPlayCount(period)
            (type == ChartType.TRACKS && sort == ChartSort.PLAYS) ->
                tracksByPlayCount(period)

            else -> throw IllegalArgumentException("unreachable")
        }
    }

    suspend fun getArtistImageUrl(artist: String) = null

    suspend fun getAlbumImageUrl(album: Album): String? {
        return networkRepository.getAlbumImageUrl(album, "large")
    }

    suspend fun getTrackImageUrl(track: Track) = null
}
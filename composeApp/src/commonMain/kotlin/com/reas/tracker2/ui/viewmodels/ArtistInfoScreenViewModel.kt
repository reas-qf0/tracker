package com.reas.tracker2.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reas.tracker2.database.Repository
import com.reas.tracker2.settings.*
import com.reas.tracker2.shared.Artist
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
    fun plays(artist: Artist, period: TimePeriod) = repository.getArtistPlays(artist, period)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = -1
        )

    fun timePlayed(artist: Artist, period: TimePeriod) = repository.getArtistTimePlayed(artist, period)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = -Duration.INFINITE
        )

    fun rank(artist: Artist, period: TimePeriod) = repository.getArtistRank(artist, period)
        .map { "#" + (it + 1).toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun playRank(artist: Artist, period: TimePeriod) = repository.getArtistRankByPlayCount(artist, period)
        .map { "#" + (it + 1).toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun topAlbums(artist: Artist, period: TimePeriod) =
        repository.getMostPlayedAlbumsFromArtist(artist, period, limit = 5)
            .map { it.map { info ->
                ChartEntryUiState(
                    label = info.album.name,
                    label2 = if (info.album.artists.size > 1) info.album.artistsAsString else null,
                    key = info.toString(),
                    metric = info.timePlayed.inMs,
                    metricAsString = info.timePlayed.inWholeMinutes.minutes.toString(),
                    bottomSheetInfo = BottomSheetInfo(album = info.album)
                )
            } }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = listOf()
            )

    fun topAlbumsByPlayCount(artist: Artist, period: TimePeriod) =
        repository.getMostPlayedAlbumsFromArtistByPlayCount(artist, period, limit = 5)
            .map { it.map { info ->
                ChartEntryUiState(
                    label = info.album.name,
                    label2 = if (info.album.artists.size > 1) info.album.artistsAsString else null,
                    key = info.toString(),
                    metric = info.playCount.toDouble(),
                    metricAsString = "${info.playCount} plays",
                    bottomSheetInfo = BottomSheetInfo(album = info.album)
                )
            } }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = listOf()
            )

    fun topTracks(artist: Artist, period: TimePeriod) =
        repository.getMostPlayedTracksFromArtist(artist, period, limit = 5)
            .map { it.map { info ->
                ChartEntryUiState(
                    label = info.track.name,
                    label2 = if (info.track.artists.size > 1) info.track.artistsAsString else null,
                    key = info.toString(),
                    metric = info.timePlayed.inMs,
                    metricAsString = info.timePlayed.inWholeMinutes.minutes.toString(),
                    bottomSheetInfo = BottomSheetInfo(track = info.track)
                )
            } }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = listOf()
            )

    fun topTracksByPlayCount(artist: Artist, period: TimePeriod) =
        repository.getMostPlayedTracksFromArtistByPlayCount(artist, period, limit = 5)
            .map { it.map { info ->
                ChartEntryUiState(
                    label = info.track.name,
                    label2 = if (info.track.artists.size > 1) info.track.artistsAsString else null,
                    key = info.toString(),
                    metric = info.playCount.toDouble(),
                    metricAsString = "${info.playCount} plays",
                    bottomSheetInfo = BottomSheetInfo(track = info.track)
                )
            } }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = listOf()
            )

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
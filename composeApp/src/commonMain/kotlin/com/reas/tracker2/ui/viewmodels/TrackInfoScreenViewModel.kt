package com.reas.tracker2.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reas.tracker2.database.Repository
import com.reas.tracker2.settings.*
import com.reas.tracker2.shared.TimePeriod
import com.reas.tracker2.shared.TrackWithAlbum
import com.reas.tracker2.ui.navigation.ChartSort
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration

class TrackInfoScreenViewModel(
    private val repository: Repository,
    private val settings: Settings,
): ViewModel() {
    fun plays(track: TrackWithAlbum, period: TimePeriod) = repository.getTrackPlays(track, period)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = -1
        )

    fun timePlayed(track: TrackWithAlbum, period: TimePeriod) = repository.getTrackTimePlayed(track, period)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = -Duration.INFINITE
        )

    fun rank(track: TrackWithAlbum, period: TimePeriod) = repository.getTrackRank(track, period)
        .map { "#" + (it + 1).toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
        )

    fun playRank(track: TrackWithAlbum, period: TimePeriod) = repository.getTrackRankByPlayCount(track, period)
        .map { "#" + (it + 1).toString() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = "..."
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
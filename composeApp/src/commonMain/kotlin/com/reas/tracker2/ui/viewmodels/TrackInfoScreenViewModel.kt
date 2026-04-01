package com.reas.tracker2.ui.viewmodels

import com.reas.tracker2.database.Repository
import com.reas.tracker2.settings.Settings
import com.reas.tracker2.settings.chartSort
import com.reas.tracker2.settings.set
import com.reas.tracker2.shared.TimePeriod
import com.reas.tracker2.shared.TrackWithAlbum
import com.reas.tracker2.ui.navigation.ChartSort
import kotlinx.coroutines.flow.map

class TrackInfoScreenViewModel(
    private val repository: Repository,
    private val settings: Settings,
): TrackerViewModel() {
    fun plays(track: TrackWithAlbum, period: TimePeriod) =
        repository.getTrackPlays(track, period).asIntStateFlow()

    fun timePlayed(track: TrackWithAlbum, period: TimePeriod) =
        repository.getTrackTimePlayed(track, period).asDurationStateFlow()

    fun rank(track: TrackWithAlbum, period: TimePeriod) =
        repository.getTrackRank(track, period)
            .map { "#" + (it + 1).toString() }
            .asStringStateFlow()

    fun playRank(track: TrackWithAlbum, period: TimePeriod) =
        repository.getTrackRankByPlayCount(track, period)
            .map { "#" + (it + 1).toString() }
            .asStringStateFlow()

    fun sort() = settings.stateFlow(chartSort)

    suspend fun setSort(sort: ChartSort) {
        settings[chartSort] = sort
    }
}
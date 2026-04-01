package com.reas.tracker2.ui.viewmodels

import com.reas.tracker2.database.Repository
import com.reas.tracker2.network.NetworkRepository
import com.reas.tracker2.shared.Play

class HistoryScreenViewModel(
    private val repository: Repository,
    private val networkRepository: NetworkRepository
): TrackerViewModel() {
    val history
        get() = pagingDataFlow { repository.getRecentPlays() }
            .mapElements { entity -> repository.playEntityToObject(entity) }

    suspend fun getImageUrl(scrobble: Play): String? {
        scrobble.asAlbumOrNull?.let { album ->
            return networkRepository.getAlbumImageUrl(album, "large")
        }
        return null
    }
}
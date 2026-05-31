package com.reas.tracker2.ui.viewmodels

import com.reas.tracker2.database.Repository
import com.reas.tracker2.network.NetworkRepository
import com.reas.tracker2.shared.Event
import com.reas.tracker2.shared.EventProcessor
import com.reas.tracker2.shared.Play
import com.reas.tracker2.shared.TrackWithAlbum

class HistoryScreenViewModel(
    private val repository: Repository,
    private val networkRepository: NetworkRepository,
    private val eventProcessor: EventProcessor,
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

    suspend fun delete(scrobble: Play) {
        eventProcessor.addTemporaryEdit(scrobble, null)
        repository.deletePlay(scrobble)
        if (scrobble.isLocal) {
            scrobble.associatedEvents.forEach { event ->
                repository.deleteEvent(scrobble.source.app, event.timestamp)
            }
        }
    }

    suspend fun edit(scrobble: Play, newMetadata: TrackWithAlbum) {
        eventProcessor.addTemporaryEdit(scrobble, newMetadata)
        repository.updatePlay(scrobble.copy(metadata = newMetadata))
        if (scrobble.isLocal) {
            scrobble.associatedEvents.forEach { event ->
                repository.updateEvent(Event(
                    metadata = newMetadata,
                    duration = scrobble.duration,
                    source = scrobble.source,
                    info = event
                ))
            }
        }
    }
}
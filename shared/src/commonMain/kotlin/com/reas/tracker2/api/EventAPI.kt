package com.reas.tracker2.api

import com.reas.tracker2.shared.Event
import com.reas.tracker2.shared.Source
import kotlinx.serialization.Serializable

@Serializable
data class EventAPI(
    val track: String,
    val artists: List<String>,
    val album: String? = null,
    val albumArtists: List<String>? = null,
    val duration: Long,
    val timestamp: Long,
    val position: Long,
    val isPlaying: Boolean,
    val client: String,
    val app: String
) {
    fun toEvent() = Event.create(
        track = track,
        artists = artists,
        album = album,
        albumArtists = albumArtists,
        timestamp = timestamp,
        duration = duration,
        isPlaying = isPlaying,
        position = position,
        source = Source.user(client = client, app = app)
    )

    companion object {
        fun fromEvent(event: Event) = EventAPI(
            track = event.track,
            artists = event.artists.map { it.name },
            album = event.album,
            albumArtists = event.albumArtists?.map { it.name },
            duration = event.duration.inWholeMilliseconds,
            timestamp = event.timestamp.toEpochMilliseconds(),
            position = event.position.inWholeMilliseconds,
            isPlaying = event.isPlaying,
            client = event.client,
            app = event.app
        )
    }
}
package com.reas.tracker2.api

import com.reas.tracker2.shared.EventInfo
import com.reas.tracker2.shared.Play
import com.reas.tracker2.shared.Source
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

@Serializable
data class EventInfoAPI(
    val timestamp: Long,
    val position: Long,
    val isPlaying: Boolean,
) {
    fun toEventInfo() = EventInfo(
        timestamp = Instant.fromEpochMilliseconds(timestamp),
        position = position.milliseconds,
        isPlaying = isPlaying
    )

    companion object {
        fun fromEventInfo(event: EventInfo) = EventInfoAPI(
            timestamp = event.timestamp.toEpochMilliseconds(),
            position = event.position.inWholeMilliseconds,
            isPlaying = event.isPlaying
        )
    }
}

@Serializable
data class PlayAPI(
    val track: String,
    val artists: List<String>,
    val album: String? = null,
    val albumArtists: List<String>? = null,
    val duration: Long,
    val timestamp: Long,
    val timePlayed: Long,
    val client: String,
    val app: String,
    val associatedEvents: List<EventInfoAPI?>
) {
    fun toPlay() = Play.create(
        track = track,
        artists = artists,
        album = album,
        albumArtists = albumArtists,
        duration = duration,
        timestamp = timestamp,
        timePlayed = timePlayed,
        source = Source.user(client = client, app = app),
        associatedEvents = associatedEvents.map { it?.toEventInfo() }.toMutableList()
    )

    companion object {
        fun fromPlay(play: Play) = PlayAPI(
            track = play.track,
            artists = play.artists.map { it.name },
            album = play.album,
            albumArtists = play.albumArtists?.map { it.name },
            duration = play.duration.inWholeMilliseconds,
            timestamp = play.timestamp.toEpochMilliseconds(),
            timePlayed = play.timePlayed.inWholeMilliseconds,
            client = play.client,
            app = play.app,
            associatedEvents = play.associatedEvents.map { it?.let { EventInfoAPI.fromEventInfo(it) } }
        )
    }
}

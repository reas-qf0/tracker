package com.reas.tracker2.shared

import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Serializable
data class Album(
    val title: String,
    val artist: String
)

@Serializable
data class Track(
    val title: String,
    val artist: String
) {
    fun withAlbum() = TrackWithOptionalAlbum(this, null)
}

@Serializable
data class TrackWithAlbum(
    val _track: Track,
    val albumO: Album
) {
    val track: String
        inline get() = _track.title
    val artist: String
        inline get() = _track.artist
    val albumArtist: String
        inline get() = albumO.artist
    val album: String
        inline get() = albumO.title
}

@Serializable
data class TrackWithOptionalAlbum(
    val _track: Track,
    val _album: Album?
) {
    val track: String
        inline get() = _track.title
    val artist: String
        inline get() = _track.artist
    val albumArtist: String?
        inline get() = _album?.artist
    val album: String?
        inline get() = _album?.title
}

@Serializable
data class Metadata(
    val info: TrackWithOptionalAlbum,
    val duration: Duration,
) {
    val track: String
        inline get() = info.track
    val artist: String
        inline get() = info.artist
    val albumArtist: String?
        inline get() = info.albumArtist
    val album: String?
        inline get() = info.album
}

@Serializable
data class Event(
    val metadata: Metadata,
    val timestamp: Instant,
    val position: Duration,
    val isPlaying: Boolean,
    val sourceApp: String
) {
    val track: String
        inline get() = metadata.track
    val artist: String
        inline get() = metadata.artist
    val albumArtist: String?
        inline get() = metadata.albumArtist
    val album: String?
        inline get() = metadata.album
    val duration: Duration
        inline get() = metadata.duration
}

@Serializable
data class Source(
    val device: String,
    val app: String
) {
    companion object {
        fun local(app: String) = Source("", app)
    }
}

@Serializable
data class Play(
    val metadata: Metadata,
    val timestamp: Instant,
    var timePlayed: Duration,
    val source: Source,
    val associatedEvents: MutableList<EventInfo?>
) {
    val track: String
        inline get() = metadata.track
    val artist: String
        inline get() = metadata.artist
    val albumArtist: String?
        inline get() = metadata.albumArtist
    val album: String?
        inline get() = metadata.album
    val duration: Duration
        inline get() = metadata.duration
    val sourceDevice: String
        inline get() = source.device
    val sourceApp: String
        inline get() = source.app


    val lastTimestamp
        get() = associatedEvents.lastOrNull()?.timestamp ?: timestamp
    val lastPosition
        get() = associatedEvents.lastOrNull()?.position ?: duration
    val lastPlaying
        get() = associatedEvents.lastOrNull()?.isPlaying ?: false

    val currentPosition
        get() = lastPosition + (timestamp - lastTimestamp)
    val endTimestamp
        get() = lastTimestamp + (duration - lastPosition)

    val isNowPlaying
        get() = lastPlaying && currentPosition <= duration
    val isTiny
        get() = timePlayed < 2.seconds
    val isSkip
        get() = timePlayed < duration / 2 && timePlayed < 4.minutes
    val isFull
        get() = !isNowPlaying && !isTiny && !isSkip

    val key
        get() = "$sourceDevice/$sourceApp/$timestamp"

    @Serializable
    data class EventInfo(
        val timestamp: Instant,
        val position: Duration,
        val isPlaying: Boolean,
    ) {
        companion object {
            fun fromEvent(event: Event) = EventInfo(
                timestamp = event.timestamp,
                position = event.position,
                isPlaying = event.isPlaying,
            )
        }
    }
}

@Serializable
data class TimePeriod(
    val start: Instant,
    val end: Instant
) {
    companion object {
        val ALLTIME = TimePeriod(Instant.DISTANT_PAST, Instant.DISTANT_FUTURE)
    }
}
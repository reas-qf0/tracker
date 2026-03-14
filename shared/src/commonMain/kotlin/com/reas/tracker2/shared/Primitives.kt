package com.reas.tracker2.shared

import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
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
    fun withAlbum() = TrackWithAlbum(this, null)
}

@Serializable
data class TrackWithAlbum(
    private val trackObject: Track,
    private val albumObject: Album?
) {
    val track: String
        get() = trackObject.title
    val artist: String
        get() = trackObject.artist
    val albumArtist: String?
        get() = albumObject?.artist
    val album: String?
        get() = albumObject?.title
    val asTrack: Track
        get() = trackObject
    val hasAlbum: Boolean
        get() = albumObject != null
    val asAlbum: Album
        get() = albumObject!!
    val asAlbumOrNull: Album?
        get() =  albumObject
}
@Serializable
data class Metadata(
    private val trackObject: TrackWithAlbum,
    val duration: Duration,
) {
    val track: String
        get() = trackObject.track
    val artist: String
        get() = trackObject.artist
    val albumArtist: String?
        get() = trackObject.albumArtist
    val album: String?
        get() = trackObject.album
    val asTrack: Track
        get() = trackObject.asTrack
    val hasAlbum: Boolean
        get() = trackObject.hasAlbum
    val asAlbum: Album
        get() = trackObject.asAlbum
    val asAlbumOrNull: Album?
        get() =  trackObject.asAlbumOrNull
    val asTrackWithAlbum: TrackWithAlbum
        get() = trackObject
}

@Serializable
data class Event(
    val metadata: Metadata,
    val timestamp: Instant,
    val position: Duration,
    val isPlaying: Boolean,
    val source: Source
) {
    val track: String
        get() = metadata.track
    val artist: String
        get() = metadata.artist
    val albumArtist: String?
        get() = metadata.albumArtist
    val album: String?
        get() = metadata.album
    val duration: Duration
        get() = metadata.duration
    val user: String
        get() = source.user
    val device: String
        get() = source.device
    val app: String
        get() = source.app

    companion object {
        fun create(
            track: String,
            artist: String,
            album: String?,
            albumArtist: String?,
            duration: Long,
            timestamp: Long,
            position: Long,
            isPlaying: Boolean,
            source: Source
        ) = Event(
            metadata = Metadata(
                trackObject = TrackWithAlbum(
                    trackObject = Track(
                        title = track,
                        artist = artist
                    ),
                    albumObject = if (album != null) Album(
                        title = album,
                        artist = albumArtist ?: artist
                    ) else null,
                ),
                duration = duration.milliseconds
            ),
            timestamp = Instant.fromEpochMilliseconds(timestamp),
            position = position.milliseconds,
            isPlaying = isPlaying,
            source = source
        )
    }
}

@Serializable
data class Source(
    val user: String,
    val device: String,
    val app: String
) {
    companion object {
        fun user(device: String, app: String) = Source("", device, app)
        fun local(app: String) = Source("", "", app)
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
        get() = metadata.track
    val artist: String
        get() = metadata.artist
    val albumArtist: String?
        get() = metadata.albumArtist
    val album: String?
        get() = metadata.album
    val duration: Duration
        get() = metadata.duration
    val sourceUser: String
        get() = source.user
    val sourceDevice: String
        get() = source.device
    val sourceApp: String
        get() = source.app

    val asAlbumOrNull: Album?
        get() = metadata.asAlbumOrNull
    val asTrackWithAlbum: TrackWithAlbum
        get() = metadata.asTrackWithAlbum


    val lastTimestamp
        get() = associatedEvents.lastOrNull()?.timestamp ?: endTimestamp
    val lastPosition
        get() = associatedEvents.lastOrNull()?.position ?: duration
    val lastPlaying
        get() = associatedEvents.lastOrNull()?.isPlaying ?: false

    val currentPosition
        get() = lastPosition + (timestamp - lastTimestamp)
    val endTimestamp
        get() = associatedEvents.filterNotNull().last().timestamp + (duration - lastPosition)

    val isNowPlaying
        get() = lastPlaying && currentPosition <= duration
    val isTiny
        get() = timePlayed < EventProcessor.SKIP_MIN_DURATION
    val isSkip
        get() = timePlayed < duration / 2 && timePlayed < 4.minutes
    val isFull
        get() = !isNowPlaying && !isTiny && !isSkip

    val key
        get() = "$sourceDevice/$sourceApp/$timestamp"

    companion object {
        fun fromEvent(event: Event): Play = Play(
            metadata = event.metadata,
            timestamp = event.timestamp,
            timePlayed = Duration.ZERO,
            source = event.source,
            associatedEvents = mutableListOf(EventInfo.fromEvent(event))
        )

        fun create(
            track: String,
            artist: String,
            album: String?,
            albumArtist: String?,
            duration: Long,
            timestamp: Long,
            timePlayed: Long,
            source: Source,
            associatedEvents: MutableList<EventInfo?>
        ) = Play(
            metadata = Metadata(
                trackObject = TrackWithAlbum(
                    trackObject = Track(
                        title = track,
                        artist = artist
                    ),
                    albumObject = if (album != null) Album(
                        title = album,
                        artist = albumArtist ?: artist
                    ) else null,
                ),
                duration = duration.milliseconds
            ),
            timePlayed = timePlayed.milliseconds,
            timestamp = Instant.fromEpochMilliseconds(timestamp),
            source = source,
            associatedEvents = associatedEvents
        )
    }

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
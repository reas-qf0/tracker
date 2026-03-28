package com.reas.tracker2.shared

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Serializable
data class Artist(
    val name: String,
    @Transient val id: Long = -1,
)

@Serializable
data class Album(
    val name: String,
    val artists: List<Artist>,
    @Transient val id: Long = -1,
) {
    val artistsAsString: String
        get() = artists.joinToString(", ") { it.name }
}

@Serializable
data class Track(
    val name: String,
    val artists: List<Artist>,
    @Transient val id: Long = -1,
) {
    val artistsAsString: String
        get() = artists.joinToString(", ") { it.name }
    fun withAlbum() = TrackWithAlbum(this, null)
}

@Serializable
data class TrackWithAlbum(
    private val trackObject: Track,
    private val albumObject: Album?
) {
    constructor(track: String, artists: List<Artist>, album: String?, albumArtists: List<Artist>?) :
            this(
                Track(track, artists),
                album?.let { Album(album, albumArtists!!) }
            )

    val id: Long
        get() = trackObject.id
    val name: String
        get() = trackObject.name
    val artists: List<Artist>
        get() = trackObject.artists
    val albumArtists: List<Artist>?
        get() = albumObject?.artists
    val artistsAsString: String
        get() = trackObject.artistsAsString
    val albumArtistsAsString: String?
        get() = albumObject?.artistsAsString
    val album: String?
        get() = albumObject?.name
    val asTrack: Track
        get() = trackObject
    val hasAlbum: Boolean
        get() = albumObject != null
    val asAlbum: Album
        get() = albumObject!!
    val asAlbumOrNull: Album?
        get() = albumObject
}

@Serializable
data class Event(
    val metadata: TrackWithAlbum,
    val duration: Duration,
    val timestamp: Instant,
    val position: Duration,
    val isPlaying: Boolean,
    val source: Source
) {
    val track: String
        get() = metadata.name
    val artists: List<Artist>
        get() = metadata.artists
    val albumArtists: List<Artist>?
        get() = metadata.albumArtists
    val artistsAsString: String
        get() = metadata.artistsAsString
    val albumArtistsAsString: String?
        get() = metadata.albumArtistsAsString
    val album: String?
        get() = metadata.album
    val user: String
        get() = source.user
    val device: String
        get() = source.device
    val app: String
        get() = source.app

    companion object {
        fun create(
            track: String,
            artists: List<Artist>,
            album: String?,
            albumArtists: List<Artist>?,
            duration: Long,
            timestamp: Long,
            position: Long,
            isPlaying: Boolean,
            source: Source
        ) = Event(
            metadata = TrackWithAlbum(track, artists, album, albumArtists ?: artists),
            duration = duration.milliseconds,
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
    val metadata: TrackWithAlbum,
    val duration: Duration,
    val timestamp: Instant,
    var timePlayed: Duration,
    val source: Source,
    val associatedEvents: MutableList<EventInfo?>,
    val id: Long? = null
) {
    val track: String
        get() = metadata.name
    val artists: List<Artist>
        get() = metadata.artists
    val albumArtists: List<Artist>?
        get() = metadata.albumArtists
    val artistsAsString: String
        get() = metadata.artistsAsString
    val albumArtistsAsString: String?
        get() = metadata.albumArtistsAsString
    val album: String?
        get() = metadata.album
    val sourceUser: String
        get() = source.user
    val sourceDevice: String
        get() = source.device
    val sourceApp: String
        get() = source.app

    val asAlbumOrNull: Album?
        get() = metadata.asAlbumOrNull


    val lastTimestamp
        get() = associatedEvents.lastOrNull()?.timestamp ?: endTimestamp
    val lastPosition
        get() = associatedEvents.lastOrNull()?.position ?: duration
    val lastPlaying
        get() = associatedEvents.lastOrNull()?.isPlaying ?: false

    val currentPosition
        get() = lastPosition + (timestamp - lastTimestamp)
    val endTimestamp
        get() = associatedEvents.filterNotNull().last().timestamp + (duration - associatedEvents.filterNotNull().last().position)

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
        fun fromEvent(event: Event, id: Long? = null): Play = Play(
            metadata = event.metadata,
            duration = event.duration,
            timestamp = event.timestamp,
            timePlayed = Duration.ZERO,
            source = event.source,
            associatedEvents = mutableListOf(EventInfo.fromEvent(event)),
            id = id
        )

        fun create(
            track: String,
            artists: List<Artist>,
            album: String?,
            albumArtists: List<Artist>?,
            duration: Long,
            timestamp: Long,
            timePlayed: Long,
            source: Source,
            associatedEvents: MutableList<EventInfo?>,
            id: Long? = null
        ) = Play(
            metadata = TrackWithAlbum(track, artists, album, albumArtists ?: artists),
            duration = duration.milliseconds,
            timePlayed = timePlayed.milliseconds,
            timestamp = Instant.fromEpochMilliseconds(timestamp),
            source = source,
            associatedEvents = associatedEvents,
            id = id
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
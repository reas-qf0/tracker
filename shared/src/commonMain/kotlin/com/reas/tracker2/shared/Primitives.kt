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
) {
    override fun equals(other: Any?): Boolean {
        return other is Artist && name == other.name
    }
}

@Serializable
data class Album(
    val name: String,
    val artists: List<Artist>,
    @Transient val id: Long = -1,
) {
    val artistsAsString: String
        get() = artists.joinToString(", ") { it.name }

    override fun equals(other: Any?): Boolean {
        return other is Album && name == other.name && artists == other.artists
    }
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

    override fun equals(other: Any?): Boolean {
        return other is Track && name == other.name && artists == other.artists
    }
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

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is TrackWithAlbum) return false
        if (this === other) return true

        if (name != other.name) return false
        if (artists != other.artists) return false
        if (album != other.album) return false
        if (albumArtists != other.albumArtists) return false
        return true
    }
}

@Serializable
enum class EventState {
    PLAYING,
    STOPPED,
    PLUGGED
}

@Serializable
data class EventInfo(
    val timestamp: Instant,
    val position: Duration,
    val state: EventState,
)

@Serializable
data class Event(
    val metadata: TrackWithAlbum,
    val duration: Duration,
    val info: EventInfo,
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
    val client: String
        get() = source.client
    val app: String
        get() = source.app
    val timestamp: Instant
        get() = info.timestamp
    val position: Duration
        get() = info.position
    val isPlaying: Boolean
        get() = info.state == EventState.PLAYING
    val state: EventState
        get() = info.state

    companion object {
        fun create(
            track: String,
            artists: List<String>,
            album: String?,
            albumArtists: List<String>?,
            duration: Long,
            timestamp: Long,
            position: Long,
            state: EventState,
            source: Source
        ) = Event(
            metadata = TrackWithAlbum(
                track,
                artists.map { Artist(it) },
                album,
                (albumArtists ?: artists).map { Artist(it) }
            ),
            duration = duration.milliseconds,
            info = EventInfo(
                timestamp = Instant.fromEpochMilliseconds(timestamp),
                position = position.milliseconds,
                state = state,
            ),
            source = source
        )
    }
}

@Serializable
data class Source(
    val user: String,
    val client: String,
    val app: String
) {
    companion object {
        fun user(client: String, app: String) = Source("", client, app)
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
    val associatedEvents: MutableList<EventInfo>,
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
    val user: String
        get() = source.user
    val client: String
        get() = source.client
    val app: String
        get() = source.app

    val asAlbumOrNull: Album?
        get() = metadata.asAlbumOrNull


    val lastTimestamp
        get() = associatedEvents.last().timestamp
    val lastPosition
        get() = associatedEvents.last().position
    val lastPlaying
        get() = associatedEvents.last().state == EventState.PLAYING

    val currentPosition
        get() = lastPosition + (timestamp - lastTimestamp)
    val endTimestamp
        get() = associatedEvents.last().let { lastEvent ->
            lastEvent.timestamp + (duration - lastEvent.position)
        }

    val isNowPlaying
        get() = lastPlaying && currentPosition <= duration
    val isTiny
        get() = timePlayed < EventProcessor.SKIP_MIN_DURATION
    val isSkip
        get() = timePlayed < duration / 2 && timePlayed < 4.minutes
    val isFull
        get() = !isNowPlaying && !isTiny && !isSkip

    val key
        get() = "$client/$app/$timestamp"

    companion object {
        fun fromEvent(event: Event, id: Long? = null): Play = Play(
            metadata = event.metadata,
            duration = event.duration,
            timestamp = event.timestamp,
            timePlayed = Duration.ZERO,
            source = event.source,
            associatedEvents = mutableListOf(event.info),
            id = id
        )

        fun create(
            track: String,
            artists: List<String>,
            album: String?,
            albumArtists: List<String>?,
            duration: Long,
            timestamp: Long,
            timePlayed: Long,
            source: Source,
            associatedEvents: MutableList<EventInfo>,
            id: Long? = null
        ) = Play(
            metadata = TrackWithAlbum(
                track,
                artists.map { Artist(it) },
                album,
                (albumArtists ?: artists).map { Artist(it) }
            ),
            duration = duration.milliseconds,
            timePlayed = timePlayed.milliseconds,
            timestamp = Instant.fromEpochMilliseconds(timestamp),
            source = source,
            associatedEvents = associatedEvents,
            id = id
        )
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
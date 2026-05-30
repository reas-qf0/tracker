package com.reas.tracker2.database

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.reas.tracker2.database.entities.*
import com.reas.tracker2.shared.*
import kotlin.time.Duration
import kotlin.time.Instant

data class AlbumWithData(
    @Embedded val album: AlbumEntity,
    @Relation(
        parentColumn = "albumId",
        entityColumn = "artistId",
        associateBy = Junction(AlbumArtistCrossRef::class)
    )
    val artists: List<ArtistEntity>
) {
    fun toAlbum() = Album(
        id = album.albumId,
        name = album.name,
        artists = artists.map { Artist(it.name, it.artistId) }
    )
}

data class TrackWithData(
    @Embedded val track: TrackEntity,
    @Relation(
        parentColumn = "trackId",
        entityColumn = "artistId",
        associateBy = Junction(TrackArtistCrossRef::class)
    )
    val artists: List<ArtistEntity>,
    @Relation(
        entity = AlbumEntity::class,
        parentColumn = "albumId",
        entityColumn = "albumId"
    )
    val album: AlbumWithData?
) {
    fun toTrack() = TrackWithAlbum(
        trackObject = Track(
            id = track.trackId,
            name = track.name,
            artists = artists.map { Artist(it.name, it.artistId) }
        ),
        albumObject = if (album == null) null else Album(
            id = album.album.albumId,
            name = album.album.name,
            artists = album.artists.map { Artist(it.name, it.artistId) }
        )
    )
}

data class PlayWithData(
    @Embedded val metadata: TrackWithData,
    val timestamp: Instant,
    val duration: Duration,
    var timePlayed: Duration,
    var lastPosition: Duration,
    var lastPlaying: Boolean,
    val sourceDevice: String,
    val sourceApp: String,
    val associatedEvents: MutableList<EventInfo>
)

data class ArtistWithPlayCount(
    @Embedded val artist: Artist,
    val playCount: Int
)

data class ArtistWithTimePlayed(
    @Embedded val artist: Artist,
    val timePlayed: Duration
)

data class TrackWithPlayCount(
    @Embedded val _track: TrackWithData,
    val playCount: Int
) {
    val track: TrackWithAlbum
        get() = _track.toTrack()
}

data class TrackWithTimePlayed(
    @Embedded val _track: TrackWithData,
    val timePlayed: Duration
) {
    val track: TrackWithAlbum
        get() = _track.toTrack()
}

data class AlbumWithPlayCount(
    @Embedded val _album: AlbumWithData,
    val playCount: Int
) {
    val album: Album
        get() = _album.toAlbum()
}

data class AlbumWithTimePlayed(
    @Embedded val _album: AlbumWithData,
    val timePlayed: Duration
) {
    val album: Album
        get() = _album.toAlbum()
}
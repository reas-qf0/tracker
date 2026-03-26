package com.reas.tracker2.database

import androidx.room.ColumnInfo
import com.reas.tracker2.shared.Album
import com.reas.tracker2.shared.TrackWithAlbum
import kotlin.time.Duration

// helper classes to pack information from db responses
// ideally shouldn't exist but idk how to get rid of them yet

data class ArtistWithPlayCount(
    val artist: String,
    val playCount: Int
)

data class ArtistWithTimePlayed(
    val artist: String,
    val timePlayed: Duration
)

data class TrackWithPlayCount(
    @ColumnInfo(name = "artist")
    val _artist: String,
    @ColumnInfo(name = "track")
    val _track: String,
    @ColumnInfo(name = "albumArtist")
    val _albumArtist: String,
    @ColumnInfo(name = "album")
    val _album: String,
    val playCount: Int
) {
    val track: TrackWithAlbum
        get() = TrackWithAlbum(_track, _artist, _album, _albumArtist)
}

data class TrackWithTimePlayed(
    @ColumnInfo(name = "artist")
    val _artist: String,
    @ColumnInfo(name = "track")
    val _track: String,
    @ColumnInfo(name = "albumArtist")
    val _albumArtist: String,
    @ColumnInfo(name = "album")
    val _album: String,
    val timePlayed: Duration
) {
    val track: TrackWithAlbum
        get() = TrackWithAlbum(_track, _artist, _album, _albumArtist)
}

data class AlbumWithPlayCount(
    @ColumnInfo(name = "albumArtist")
    val _artist: String,
    @ColumnInfo(name = "album")
    val _album: String,
    val playCount: Int
) {
    val album: Album
        get() = Album(_album, _artist)
}

data class AlbumWithTimePlayed(
    @ColumnInfo(name = "albumArtist")
    val _artist: String,
    @ColumnInfo(name = "album")
    val _album: String,
    val timePlayed: Duration
) {
    val album: Album
        get() = Album(_album, _artist)
}
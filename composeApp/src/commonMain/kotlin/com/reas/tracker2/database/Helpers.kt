package com.reas.tracker2.database

import androidx.room.ColumnInfo
import com.reas.tracker2.shared.Album
import com.reas.tracker2.shared.Track
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
    val playCount: Int
) {
    val track: Track
        get() = Track(_track, _artist)
}

data class TrackWithTimePlayed(
    @ColumnInfo(name = "artist")
    val _artist: String,
    @ColumnInfo(name = "track")
    val _track: String,
    val timePlayed: Duration
) {
    val track: Track
        get() = Track(_track, _artist)
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
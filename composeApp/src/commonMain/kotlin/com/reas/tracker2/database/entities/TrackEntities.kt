package com.reas.tracker2.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey(autoGenerate = true)
    val trackId: Long = 0,
    val name: String,
    val albumId: Long? = null,
    val artistIds: List<Long>,
)

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true)
    val albumId: Long = 0,
    val name: String,
    val artistIds: List<Long>,
)

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey(autoGenerate = true)
    val artistId: Long = 0,
    val name: String,
)

@Entity(
    tableName = "track_artists",
    primaryKeys = ["trackId", "artistId"],
    indices = [Index(value = ["artistId"])]
)
data class TrackArtistCrossRef(
    val trackId: Long,
    val artistId: Long
)

@Entity(
    tableName = "album_artists",
    primaryKeys = ["albumId", "artistId"],
    indices = [Index(value = ["artistId"])]
)
data class AlbumArtistCrossRef(
    val albumId: Long,
    val artistId: Long
)
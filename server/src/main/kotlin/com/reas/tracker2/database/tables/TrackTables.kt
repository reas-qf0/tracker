package com.reas.tracker2.database.tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object ArtistTable : LongIdTable("artists") {
    val name = text("name")
}

object AlbumTable : LongIdTable("albums") {
    val name = text("name")
    val artistIds = text("artistIds")
}

object TrackTable : LongIdTable("tracks") {
    val name = text("name")
    val albumId = reference("albumId", AlbumTable.id).nullable()
    val artistIds = text("artistIds")
}

object AlbumArtistCrossRefTable : Table("album_artists") {
    val albumId = reference("albumId", AlbumTable.id)
    val artistId = reference("artistId", ArtistTable.id)
}

object TrackArtistCrossRefTable : Table("track_artists") {
    val trackId = reference("trackId", TrackTable.id)
    val artistId = reference("artistId", ArtistTable.id)
}
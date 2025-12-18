package org.reas.tracker.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class TrackInfo(
    val artist: String,
    val track: String
)

data class AlbumInfo(
    @ColumnInfo(name = "albumArtist")
    val artist: String,
    val album: String
)

@Dao
interface PlayDao {
    @Insert
    suspend fun insert(play: Play): Long

    @Update
    suspend fun update(play: Play)

    @Delete
    suspend fun delete(play: Play)

    @Query("SELECT * FROM plays WHERE playerId = :playerid ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastPlayFromPlayer(playerid: String): Play?

    @Query("DELETE FROM plays WHERE playerId = :playerId")
    suspend fun clearPlaysFromPlayer(playerId: String)

    @Query("SELECT * FROM plays WHERE state = ${Play.PLAYING}")
    fun getNowPlayingTracks(): Flow<List<Play>>

    @Query("SELECT * FROM plays WHERE state = ${Play.FULL} ORDER BY timestamp DESC LIMIT :amount")
    fun getRecentPlays(amount: Int): Flow<List<Play>>

    @Query("SELECT COUNT(*) FROM plays WHERE artist = :artist AND state = ${Play.FULL}")
    fun getArtistPlays(artist: String): Flow<Int>

    @Query("SELECT SUM(timePlayed) FROM plays WHERE artist = :artist AND state != ${Play.TINY}")
    fun getArtistTimePlayed(artist: String): Flow<Long>

    @Query("SELECT artist FROM plays WHERE timestamp >= :start AND timestamp < :end " +
            "GROUP BY artist ORDER BY SUM(timePlayed) DESC LIMIT :amount")
    fun getMostPlayedArtists(start: Long, end: Long, amount: Int): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM plays WHERE artist = :artist AND track = :track AND state = ${Play.FULL}")
    fun getTrackPlays(artist: String, track: String): Flow<Int>

    @Query("SELECT SUM(timePlayed) FROM plays WHERE artist = :artist AND track = :track AND state != ${Play.TINY}")
    fun getTrackTimePlayed(artist: String, track: String): Flow<Long>

    @Query("SELECT artist, track FROM plays WHERE timestamp >= :start AND timestamp < :end " +
            "GROUP BY artist, track ORDER BY SUM(timePlayed) DESC LIMIT :amount")
    fun getMostPlayedTracks(start: Long, end: Long, amount: Int): Flow<List<TrackInfo>>

    @Query("SELECT COUNT(*) FROM plays WHERE album = :album AND albumArtist = :artist AND state = ${Play.FULL}")
    fun getAlbumPlays(artist: String, album: String): Flow<Int>

    @Query("SELECT SUM(timePlayed) FROM plays WHERE album = :album AND albumArtist = :artist AND state != ${Play.TINY}")
    fun getAlbumTimePlayed(artist: String, album: String): Flow<Long>

    @Query("SELECT albumArtist, album FROM plays WHERE timestamp >= :start AND timestamp < :end " +
            "GROUP BY albumArtist, album ORDER BY SUM(timePlayed) DESC LIMIT :amount")
    fun getMostPlayedAlbums(start: Long, end: Long, amount: Int): Flow<List<AlbumInfo>>
}
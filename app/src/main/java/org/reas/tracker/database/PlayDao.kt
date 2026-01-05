package org.reas.tracker.database

import androidx.paging.PagingSource
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class ArtistInfo(
    val artist: String,
    val metric: Long
)

data class TrackInfo(
    val artist: String,
    val track: String,
    val metric: Long
)

data class AlbumInfo(
    @ColumnInfo(name = "albumArtist")
    val artist: String,
    val album: String,
    val metric: Long
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

    @Query("SELECT * FROM plays WHERE lastPlaying = 1")
    fun getNowPlayingTracks(): Flow<List<Play>>

    @Query("SELECT * FROM plays WHERE timePlayed >= MIN(duration / 2, 240 * 1000) ORDER BY timestamp DESC")
    fun getRecentPlays(): PagingSource<Int, Play>

    @Query("SELECT COUNT(*) FROM plays WHERE artist = :artist AND timePlayed >= MIN(duration / 2, 240 * 1000)")
    fun getArtistPlays(artist: String): Flow<Int>

    @Query("SELECT SUM(timePlayed) FROM plays WHERE artist = :artist " +
            "AND timePlayed >= ${EventProcessor.SKIP_MIN_DURATION}")
    fun getArtistTimePlayed(artist: String): Flow<Long>

    @Query("SELECT artist, SUM(timePlayed) as metric FROM plays " +
            "WHERE timestamp >= :start AND timestamp < :end " +
            "GROUP BY artist ORDER BY metric DESC")
    fun getMostPlayedArtists(start: Long, end: Long): PagingSource<Int, ArtistInfo>

    @Query("SELECT artist, COUNT(*) as metric FROM plays " +
            "WHERE timestamp >= :start AND timestamp < :end AND timePlayed >= MIN(duration / 2, 240 * 1000)" +
            "GROUP BY artist ORDER BY metric DESC")
    fun getMostPlayedArtistsByPlayCount(start: Long, end: Long): PagingSource<Int, ArtistInfo>

    @Query("SELECT COUNT(*) FROM plays WHERE artist = :artist AND track = :track " +
            "AND (:album IS NULL OR album = :album) " +
            "AND timePlayed >= MIN(duration / 2, 240 * 1000)")
    fun getTrackPlays(artist: String, track: String, album: String?): Flow<Int>

    @Query("SELECT SUM(timePlayed) FROM plays WHERE artist = :artist AND track = :track " +
            "AND (:album IS NULL OR album = :album) " +
            "AND timePlayed >= ${EventProcessor.SKIP_MIN_DURATION}")
    fun getTrackTimePlayed(artist: String, track: String, album: String?): Flow<Long>

    @Query("SELECT * FROM plays WHERE artist = :artist AND track = :track " +
            "AND (:album IS NULL OR album = :album) " +
            "AND timePlayed >= MIN(duration / 2, 240 * 1000) ORDER BY timestamp DESC")
    fun getTrackHistory(artist: String, track: String, album: String?): PagingSource<Int, Play>

    @Query("SELECT artist, track, SUM(timePlayed) as metric FROM plays " +
            "WHERE timestamp >= :start AND timestamp < :end " +
            "GROUP BY artist, track ORDER BY metric DESC")
    fun getMostPlayedTracks(start: Long, end: Long): PagingSource<Int, TrackInfo>

    @Query("SELECT artist, track, COUNT(*) as metric FROM plays " +
            "WHERE timestamp >= :start AND timestamp < :end AND timePlayed >= MIN(duration / 2, 240 * 1000)" +
            "GROUP BY artist, track ORDER BY metric DESC")
    fun getMostPlayedTracksByPlayCount(start: Long, end: Long): PagingSource<Int, TrackInfo>

    @Query("SELECT COUNT(*) FROM plays WHERE album = :album AND albumArtist = :artist " +
            "AND timePlayed >= MIN(duration / 2, 240 * 1000)")
    fun getAlbumPlays(artist: String, album: String): Flow<Int>

    @Query("SELECT SUM(timePlayed) FROM plays WHERE album = :album AND albumArtist = :artist " +
            "AND timePlayed >= ${EventProcessor.SKIP_MIN_DURATION}")
    fun getAlbumTimePlayed(artist: String, album: String): Flow<Long>

    @Query("SELECT albumArtist, album, SUM(timePlayed) as metric FROM plays " +
            "WHERE timestamp >= :start AND timestamp < :end AND NOT (album IS NULL)" +
            "GROUP BY albumArtist, album ORDER BY metric DESC")
    fun getMostPlayedAlbums(start: Long, end: Long): PagingSource<Int, AlbumInfo>

    @Query("SELECT albumArtist, album, COUNT(*) as metric FROM plays " +
            "WHERE timestamp >= :start AND timestamp < :end AND timePlayed >= MIN(duration / 2, 240 * 1000) AND NOT (album IS NULL)" +
            "GROUP BY albumArtist, album ORDER BY metric DESC")
    fun getMostPlayedAlbumsByPlayCount(start: Long, end: Long): PagingSource<Int, AlbumInfo>
}
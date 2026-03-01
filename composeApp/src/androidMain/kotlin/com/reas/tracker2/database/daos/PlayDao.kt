package com.reas.tracker2.database.daos

import androidx.paging.PagingSource
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.reas.tracker2.database.objects.Play
import com.reas.tracker2.util.EventProcessor
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


private const val inTimeRange = "timestamp >= :start AND timestamp < :end"
private const val isFullPlay = "timePlayed >= MIN(duration / 2, 240 * 1000)"
private const val isNotSkip = "timePlayed >= ${EventProcessor.SKIP_MIN_DURATION}"
private const val matchesArtist = "artist = :artist"
private const val matchesAlbum = "albumArtist = :artist AND album = :album"
private const val matchesTrack = "artist = :artist AND track = :track AND (:album IS NULL OR album = :album)"
private const val hasAlbum = "NOT (album IS NULL)"
private const val sourceMatches = "sourceDevice = :device AND sourceApp = :app"

@Dao
interface PlayDao {
    @Insert(onConflict = REPLACE)
    suspend fun insert(play: Play): Long

    @Update
    suspend fun update(play: Play)

    @Delete
    suspend fun delete(play: Play)

    @Query("SELECT * FROM plays WHERE $sourceMatches ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastPlayFromSource(device: String?, app: String?): Play?

    @Query("SELECT * FROM plays WHERE lastPlaying = 1")
    fun getNowPlayingTracks(): Flow<List<Play>>

    @Query("SELECT * FROM plays WHERE lastPlaying = 1 UNION SELECT * FROM plays WHERE $isFullPlay AND lastPlaying = 0 ORDER BY timestamp DESC")
    fun getRecentPlays(): PagingSource<Int, Play>

    @Query("SELECT COUNT(*) FROM plays " +
            "WHERE $matchesArtist AND $inTimeRange AND $isFullPlay")
    fun getArtistPlays(artist: String, start: Long, end: Long): Flow<Int>

    @Query("SELECT SUM(timePlayed) FROM plays " +
            "WHERE $matchesArtist AND $inTimeRange AND $isNotSkip")
    fun getArtistTimePlayed(artist: String, start: Long, end: Long): Flow<Long>

    @Query("SELECT artist, SUM(timePlayed) as metric FROM plays " +
            "WHERE $inTimeRange AND $isNotSkip " +
            "GROUP BY artist ORDER BY metric DESC")
    fun getMostPlayedArtists(start: Long, end: Long): PagingSource<Int, ArtistInfo>

    @Query("SELECT artist, COUNT(*) as metric FROM plays " +
            "WHERE $inTimeRange AND $isFullPlay" +
            "GROUP BY artist ORDER BY metric DESC")
    fun getMostPlayedArtistsByPlayCount(start: Long, end: Long): PagingSource<Int, ArtistInfo>

    @Query("WITH t0 AS (" +
            "SELECT SUM(timePlayed) as metric FROM plays " +
            "WHERE $inTimeRange AND $isNotSkip GROUP BY artist" +
            ") SELECT COUNT(*) FROM t0 WHERE metric > :time")
    fun getArtistRank(time: Long, start: Long, end: Long): Flow<Int>

    @Query("WITH t0 AS (" +
            "SELECT COUNT(*) as metric FROM plays " +
            "WHERE $inTimeRange AND $isFullPlay GROUP BY artist" +
            ") SELECT COUNT(*) FROM t0 WHERE metric > :count")
    fun getArtistRankByPlayCount(count: Int, start: Long, end: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM plays " +
            "WHERE $matchesTrack AND $inTimeRange AND $isFullPlay")
    fun getTrackPlays(artist: String, track: String, album: String?, start: Long, end: Long): Flow<Int>

    @Query("SELECT SUM(timePlayed) FROM plays " +
            "WHERE $matchesTrack AND $inTimeRange AND $isNotSkip")
    fun getTrackTimePlayed(artist: String, track: String, album: String?, start: Long, end: Long): Flow<Long>

    @Query("SELECT * FROM plays " +
            "WHERE $matchesTrack AND $isFullPlay " +
            "ORDER BY timestamp DESC")
    fun getTrackHistory(artist: String, track: String, album: String?): PagingSource<Int, Play>

    @Query("SELECT artist, track, SUM(timePlayed) as metric FROM plays " +
            "WHERE $inTimeRange AND $isNotSkip " +
            "GROUP BY artist, track ORDER BY metric DESC")
    fun getMostPlayedTracks(start: Long, end: Long): PagingSource<Int, TrackInfo>

    @Query("SELECT artist, track, COUNT(*) as metric FROM plays " +
            "WHERE $inTimeRange AND $isFullPlay" +
            "GROUP BY artist, track ORDER BY metric DESC")
    fun getMostPlayedTracksByPlayCount(start: Long, end: Long): PagingSource<Int, TrackInfo>

    @Query("SELECT artist, track, SUM(timePlayed) as metric FROM plays " +
            "WHERE $inTimeRange AND $isNotSkip AND $matchesArtist " +
            "GROUP BY artist, track ORDER BY metric DESC")
    fun getMostPlayedTracksFromArtist(artist: String, start: Long, end: Long): PagingSource<Int, TrackInfo>

    @Query("SELECT artist, track, COUNT(*) as metric FROM plays " +
            "WHERE $inTimeRange AND $isFullPlay AND $matchesArtist " +
            "GROUP BY artist, track ORDER BY metric DESC")
    fun getMostPlayedTracksFromArtistByPlayCount(artist: String, start: Long, end: Long): PagingSource<Int, TrackInfo>

    @Query("SELECT artist, track, SUM(timePlayed) as metric FROM plays " +
            "WHERE $inTimeRange AND $isNotSkip AND $matchesAlbum " +
            "GROUP BY artist, track ORDER BY metric DESC")
    fun getMostPlayedTracksFromAlbum(artist: String, album: String, start: Long, end: Long): PagingSource<Int, TrackInfo>

    @Query("SELECT artist, track, COUNT(*) as metric FROM plays " +
            "WHERE $inTimeRange AND $isFullPlay AND $matchesAlbum " +
            "GROUP BY artist, track ORDER BY metric DESC")
    fun getMostPlayedTracksFromAlbumByPlayCount(artist: String, album: String, start: Long, end: Long): PagingSource<Int, TrackInfo>

    @Query("WITH t0 AS (" +
            "SELECT SUM(timePlayed) as metric FROM plays " +
            "WHERE $inTimeRange AND $isNotSkip GROUP BY artist, track" +
            ") SELECT COUNT(*) FROM t0 WHERE metric > :time")
    fun getTrackRank(time: Long, start: Long, end: Long): Flow<Int>

    @Query("WITH t0 AS (" +
            "SELECT COUNT(*) as metric FROM plays " +
            "WHERE $inTimeRange AND $isFullPlay GROUP BY artist, track" +
            ") SELECT COUNT(*) FROM t0 WHERE metric > :count")
    fun getTrackRankByPlayCount(count: Int, start: Long, end: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM plays WHERE $matchesAlbum " +
            "AND $inTimeRange AND $isFullPlay")
    fun getAlbumPlays(artist: String, album: String, start: Long, end: Long): Flow<Int>

    @Query("SELECT SUM(timePlayed) FROM plays " +
            "WHERE $matchesAlbum AND $inTimeRange AND $isNotSkip")
    fun getAlbumTimePlayed(artist: String, album: String, start: Long, end: Long): Flow<Long>

    @Query("SELECT albumArtist, album, SUM(timePlayed) as metric FROM plays " +
            "WHERE $inTimeRange AND $hasAlbum AND $isNotSkip " +
            "GROUP BY albumArtist, album ORDER BY metric DESC")
    fun getMostPlayedAlbums(start: Long, end: Long): PagingSource<Int, AlbumInfo>

    @Query("SELECT albumArtist, album, COUNT(*) as metric FROM plays " +
            "WHERE $inTimeRange AND $isFullPlay AND $hasAlbum" +
            "GROUP BY albumArtist, album ORDER BY metric DESC")
    fun getMostPlayedAlbumsByPlayCount(start: Long, end: Long): PagingSource<Int, AlbumInfo>

    @Query("SELECT albumArtist, album, SUM(timePlayed) as metric FROM plays " +
            "WHERE $inTimeRange AND $hasAlbum AND $isNotSkip AND $matchesArtist " +
            "GROUP BY albumArtist, album ORDER BY metric DESC")
    fun getMostPlayedAlbumsFromArtist(artist: String, start: Long, end: Long): PagingSource<Int, AlbumInfo>

    @Query("SELECT albumArtist, album, COUNT(*) as metric FROM plays " +
            "WHERE $inTimeRange AND $isFullPlay AND $hasAlbum AND $matchesArtist " +
            "GROUP BY albumArtist, album ORDER BY metric DESC")
    fun getMostPlayedAlbumsFromArtistByPlayCount(artist: String, start: Long, end: Long): PagingSource<Int, AlbumInfo>

    @Query("WITH t0 AS (" +
            "SELECT SUM(timePlayed) as metric FROM plays " +
            "WHERE $inTimeRange AND $isNotSkip AND $hasAlbum GROUP BY albumArtist, album" +
            ") SELECT COUNT(*) FROM t0 WHERE metric > :time")
    fun getAlbumRank(time: Long, start: Long, end: Long): Flow<Int>

    @Query("WITH t0 AS (" +
            "SELECT COUNT(*) as metric FROM plays " +
            "WHERE $inTimeRange AND $isFullPlay AND $hasAlbum GROUP BY albumArtist, album" +
            ") SELECT COUNT(*) FROM t0 WHERE metric > :count")
    fun getAlbumRankByPlayCount(count: Int, start: Long, end: Long): Flow<Int>
}
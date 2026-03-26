package com.reas.tracker2.database.daos

import androidx.paging.PagingSource
import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.REPLACE
import com.reas.tracker2.database.*
import com.reas.tracker2.database.entities.PlayEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.time.Instant


private const val inTimeRange = "timestamp >= :start AND timestamp < :end"
private const val isFullPlay = "timePlayed >= MIN(duration / 2, 240 * 1000)"
private const val isNotSkip = "timePlayed >= 2 * 1000"
private const val matchesArtist = "artist = :artist"
private const val matchesAlbum = "albumArtist = :artist AND album = :album"
private const val matchesTrack = "artist = :artist AND track = :track AND (:album IS NULL OR (album = :album AND albumArtist = :albumArtist))"
private const val hasAlbum = "NOT (album IS NULL)"
private const val sourceMatches = "sourceDevice = :device AND sourceApp = :app"

@Dao
interface PlayDao {
    @Insert(onConflict = REPLACE)
    suspend fun insert(play: PlayEntity): Long

    @Insert(onConflict = REPLACE)
    suspend fun insertBatch(plays: List<PlayEntity>)

    @Update
    suspend fun update(play: PlayEntity)

    @Delete
    suspend fun delete(play: PlayEntity)

    @Query("SELECT * FROM plays WHERE $sourceMatches ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastPlayFromSource(device: String?, app: String?): PlayEntity?

    @Query("SELECT * FROM plays WHERE lastPlaying = 1")
    fun getNowPlayingTracks(): Flow<List<PlayEntity>>

    @Query("SELECT * FROM plays WHERE lastPlaying = 1 UNION SELECT * FROM plays WHERE $isFullPlay AND lastPlaying = 0 ORDER BY timestamp DESC")
    fun getRecentPlays(): PagingSource<Int, PlayEntity>

    @Query("SELECT COUNT(*) FROM plays " +
            "WHERE $matchesArtist AND $inTimeRange AND $isFullPlay")
    fun getArtistPlays(artist: String, start: Instant, end: Instant): Flow<Int>

    @Query("SELECT SUM(timePlayed) FROM plays " +
            "WHERE $matchesArtist AND $inTimeRange AND $isNotSkip")
    fun getArtistTimePlayed(artist: String, start: Instant, end: Instant): Flow<Duration>

    @Query("SELECT artist, SUM(timePlayed) as timePlayed FROM plays " +
            "WHERE $inTimeRange AND $isNotSkip " +
            "GROUP BY artist ORDER BY timePlayed DESC")
    fun getMostPlayedArtists(start: Instant, end: Instant): PagingSource<Int, ArtistWithTimePlayed>

    @Query("SELECT artist, COUNT(*) as playCount FROM plays " +
            "WHERE $inTimeRange AND $isFullPlay" +
            "GROUP BY artist ORDER BY playCount DESC")
    fun getMostPlayedArtistsByPlayCount(start: Instant, end: Instant): PagingSource<Int, ArtistWithPlayCount>

    @Query("WITH t0 AS (" +
            "SELECT artist, SUM(timePlayed) as metric FROM plays " +
            "WHERE $inTimeRange AND $isNotSkip GROUP BY artist" +
            ") SELECT COUNT(*) FROM t0 WHERE metric > (SELECT metric FROM t0 WHERE $matchesArtist)")
    fun getArtistRank(artist: String, start: Instant, end: Instant): Flow<Int>

    @Query("WITH t0 AS (" +
            "SELECT artist, COUNT(*) as metric FROM plays " +
            "WHERE $inTimeRange AND $isFullPlay GROUP BY artist" +
            ") SELECT COUNT(*) FROM t0 WHERE metric > (SELECT metric FROM t0 WHERE $matchesArtist)")
    fun getArtistRankByPlayCount(artist: String, start: Instant, end: Instant): Flow<Int>

    @Query("SELECT COUNT(*) FROM plays " +
            "WHERE $matchesTrack AND $inTimeRange AND $isFullPlay")
    fun getTrackPlays(artist: String, track: String, album: String?, albumArtist: String?, start: Instant, end: Instant): Flow<Int>

    @Query("SELECT SUM(timePlayed) FROM plays " +
            "WHERE $matchesTrack AND $inTimeRange AND $isNotSkip")
    fun getTrackTimePlayed(artist: String, track: String, album: String?, albumArtist: String?, start: Instant, end: Instant): Flow<Duration>

    @Query("SELECT * FROM plays " +
            "WHERE $matchesTrack AND $isFullPlay " +
            "ORDER BY timestamp DESC")
    fun getTrackHistory(artist: String, track: String, album: String?, albumArtist: String?): PagingSource<Int, PlayEntity>

    @Query("SELECT artist, track, album, albumArtist, SUM(timePlayed) as timePlayed FROM plays " +
            "WHERE $inTimeRange AND $isNotSkip " +
            "GROUP BY artist, track, album, albumArtist ORDER BY timePlayed DESC")
    fun getMostPlayedTracks(start: Instant, end: Instant): PagingSource<Int, TrackWithTimePlayed>

    @Query("SELECT artist, track, album, albumArtist, COUNT(*) as playCount FROM plays " +
            "WHERE $inTimeRange AND $isFullPlay" +
            "GROUP BY artist, track, album, albumArtist ORDER BY playCount DESC")
    fun getMostPlayedTracksByPlayCount(start: Instant, end: Instant): PagingSource<Int, TrackWithPlayCount>

    @Query("SELECT artist, track, album, albumArtist, SUM(timePlayed) as timePlayed FROM plays " +
            "WHERE $inTimeRange AND $isNotSkip AND $matchesArtist " +
            "GROUP BY artist, track, album, albumArtist ORDER BY timePlayed DESC")
    fun getMostPlayedTracksFromArtist(artist: String, start: Instant, end: Instant): PagingSource<Int, TrackWithTimePlayed>

    @Query("SELECT artist, track, album, albumArtist, COUNT(*) as playCount FROM plays " +
            "WHERE $inTimeRange AND $isFullPlay AND $matchesArtist " +
            "GROUP BY artist, track, album, albumArtist ORDER BY playCount DESC")
    fun getMostPlayedTracksFromArtistByPlayCount(artist: String, start: Instant, end: Instant): PagingSource<Int, TrackWithPlayCount>

    @Query("SELECT artist, track, album, albumArtist, SUM(timePlayed) as timePlayed FROM plays " +
            "WHERE $inTimeRange AND $isNotSkip AND $matchesAlbum " +
            "GROUP BY artist, track, album, albumArtist ORDER BY timePlayed DESC")
    fun getMostPlayedTracksFromAlbum(artist: String, album: String, start: Instant, end: Instant): PagingSource<Int, TrackWithTimePlayed>

    @Query("SELECT artist, track, album, albumArtist, COUNT(*) as playCount FROM plays " +
            "WHERE $inTimeRange AND $isFullPlay AND $matchesAlbum " +
            "GROUP BY artist, track, album, albumArtist ORDER BY playCount DESC")
    fun getMostPlayedTracksFromAlbumByPlayCount(artist: String, album: String, start: Instant, end: Instant): PagingSource<Int, TrackWithPlayCount>

    @Query("WITH t0 AS (" +
            "SELECT artist, track, album, albumArtist, SUM(timePlayed) as metric FROM plays " +
            "WHERE $inTimeRange AND $isNotSkip GROUP BY artist, track, album, albumArtist" +
            ") SELECT COUNT(*) FROM t0 WHERE metric > (SELECT metric FROM t0 WHERE $matchesTrack)")
    fun getTrackRank(artist: String, track: String, album: String?, albumArtist: String?, start: Instant, end: Instant): Flow<Int>

    @Query("WITH t0 AS (" +
            "SELECT artist, track, album, albumArtist, COUNT(*) as metric FROM plays " +
            "WHERE $inTimeRange AND $isFullPlay GROUP BY artist, track, album, albumArtist" +
            ") SELECT COUNT(*) FROM t0 WHERE metric > (SELECT metric FROM t0 WHERE $matchesTrack)")
    fun getTrackRankByPlayCount(artist: String, track: String, album: String?, albumArtist: String?, start: Instant, end: Instant): Flow<Int>

    @Query("SELECT COUNT(*) FROM plays WHERE $matchesAlbum " +
            "AND $inTimeRange AND $isFullPlay")
    fun getAlbumPlays(artist: String, album: String, start: Instant, end: Instant): Flow<Int>

    @Query("SELECT SUM(timePlayed) FROM plays " +
            "WHERE $matchesAlbum AND $inTimeRange AND $isNotSkip")
    fun getAlbumTimePlayed(artist: String, album: String, start: Instant, end: Instant): Flow<Duration>

    @Query("SELECT albumArtist, album, SUM(timePlayed) as timePlayed FROM plays " +
            "WHERE $inTimeRange AND $hasAlbum AND $isNotSkip " +
            "GROUP BY albumArtist, album ORDER BY timePlayed DESC")
    fun getMostPlayedAlbums(start: Instant, end: Instant): PagingSource<Int, AlbumWithTimePlayed>

    @Query("SELECT albumArtist, album, COUNT(*) as playCount FROM plays " +
            "WHERE $inTimeRange AND $isFullPlay AND $hasAlbum" +
            "GROUP BY albumArtist, album ORDER BY playCount DESC")
    fun getMostPlayedAlbumsByPlayCount(start: Instant, end: Instant): PagingSource<Int, AlbumWithPlayCount>

    @Query("SELECT albumArtist, album, SUM(timePlayed) as timePlayed FROM plays " +
            "WHERE $inTimeRange AND $hasAlbum AND $isNotSkip AND $matchesArtist " +
            "GROUP BY albumArtist, album ORDER BY timePlayed DESC")
    fun getMostPlayedAlbumsFromArtist(artist: String, start: Instant, end: Instant): PagingSource<Int, AlbumWithTimePlayed>

    @Query("SELECT albumArtist, album, COUNT(*) as playCount FROM plays " +
            "WHERE $inTimeRange AND $isFullPlay AND $hasAlbum AND $matchesArtist " +
            "GROUP BY albumArtist, album ORDER BY playCount DESC")
    fun getMostPlayedAlbumsFromArtistByPlayCount(artist: String, start: Instant, end: Instant): PagingSource<Int, AlbumWithPlayCount>

    @Query("WITH t0 AS (" +
            "SELECT albumArtist, album, SUM(timePlayed) as metric FROM plays " +
            "WHERE $inTimeRange AND $isNotSkip AND $hasAlbum GROUP BY albumArtist, album" +
            ") SELECT COUNT(*) FROM t0 WHERE metric > (SELECT metric FROM t0 WHERE $matchesAlbum)")
    fun getAlbumRank(artist: String, album: String, start: Instant, end: Instant): Flow<Int>

    @Query("WITH t0 AS (" +
            "SELECT albumArtist, album, COUNT(*) as metric FROM plays " +
            "WHERE $inTimeRange AND $isFullPlay AND $hasAlbum GROUP BY albumArtist, album" +
            ") SELECT COUNT(*) FROM t0 WHERE metric > (SELECT metric FROM t0 WHERE $matchesAlbum)")
    fun getAlbumRankByPlayCount(artist: String, album: String, start: Instant, end: Instant): Flow<Int>

    @Query("SELECT COUNT(*) FROM plays")
    fun getPlayCount(): Flow<Int>
}
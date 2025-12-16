package org.reas.tracker.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayDao {
    @Insert
    suspend fun insert(play: Play): Long

    @Update
    suspend fun update(play: Play)

    @Delete
    suspend fun delete(play: Play)

    @Query("SELECT * FROM plays ORDER BY timestamp DESC LIMIT :amount")
    fun getRecentPlays(amount: Int): Flow<List<Play>>

    @Query("SELECT COUNT(*) FROM plays WHERE artist = :artist AND state = ${Play.FULL}")
    fun getArtistPlays(artist: String): Flow<Int>

    @Query("SELECT SUM(timePlayed) FROM plays WHERE artist = :artist AND state != ${Play.TINY}")
    fun getArtistTimePlayed(artist: String): Flow<Long>

    @Query("SELECT COUNT(*) FROM plays WHERE artist = :artist AND track = :track AND state = ${Play.FULL}")
    fun getTrackPlays(artist: String, track: String): Flow<Int>

    @Query("SELECT SUM(timePlayed) FROM plays WHERE artist = :artist AND track = :track AND state != ${Play.TINY}")
    fun getTrackTimePlayed(artist: String, track: String): Flow<Long>

    @Query("SELECT COUNT(*) FROM plays WHERE album = :album AND albumArtist = :artist AND state = ${Play.FULL}")
    fun getAlbumPlays(artist: String, album: String): Flow<Int>

    @Query("SELECT SUM(timePlayed) FROM plays WHERE album = :album AND albumArtist = :artist AND state != ${Play.TINY}")
    fun getAlbumTimePlayed(artist: String, album: String): Flow<Long>
}
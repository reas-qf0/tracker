package org.reas.tracker.database

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow

interface Repository {
    suspend fun insertEvent(event: Event): Long
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(event: Event)
    suspend fun getLastEventFromPlayer(playerId: String): Event?
    suspend fun getEventsFromPlayer(playerId: String): List<Event>
    suspend fun deleteEventsFromPlayer(playerId: String)
    suspend fun getEvents(eventIds: List<String>): List<Event>

    suspend fun insertPlay(play: Play): Long
    suspend fun updatePlay(play: Play)
    suspend fun deletePlay(play: Play)
    suspend fun getLastPlayFromPlayer(playerId: String): Play?
    suspend fun clearPlaysFromPlayer(playerId: String)
    fun getNowPlayingTracks(): Flow<List<Play>>
    fun getRecentPlays(): PagingSource<Int, Play>

    fun getArtistPlays(artist: String, start: Long, end: Long): Flow<Int>
    fun getArtistTimePlayed(artist: String, start: Long, end: Long): Flow<Long>
    fun getMostPlayedArtists(start: Long, end: Long): PagingSource<Int, ArtistInfo>
    fun getMostPlayedArtistsByPlayCount(start: Long, end: Long): PagingSource<Int, ArtistInfo>
    fun getArtistRank(time: Long, start: Long, end: Long): Flow<Int>
    fun getArtistRankByPlayCount(count: Int, start: Long, end: Long) : Flow<Int>

    fun getTrackPlays(artist: String, track: String, album: String? = null, start: Long, end: Long): Flow<Int>
    fun getTrackTimePlayed(artist: String, track: String, album: String? = null, start: Long, end: Long): Flow<Long>
    fun getTrackHistory(artist: String, track: String, album: String? = null): PagingSource<Int, Play>
    fun getMostPlayedTracks(start: Long, end: Long): PagingSource<Int, TrackInfo>
    fun getMostPlayedTracksByPlayCount(start: Long, end: Long): PagingSource<Int, TrackInfo>
    fun getMostPlayedTracksFromArtist(artist: String, start: Long, end: Long): PagingSource<Int, TrackInfo>
    fun getMostPlayedTracksFromArtistByPlayCount(artist: String, start: Long, end: Long): PagingSource<Int, TrackInfo>
    fun getMostPlayedTracksFromAlbum(artist: String, album: String, start: Long, end: Long): PagingSource<Int, TrackInfo>
    fun getMostPlayedTracksFromAlbumByPlayCount(artist: String, album: String, start: Long, end: Long): PagingSource<Int, TrackInfo>

    fun getAlbumPlays(artist: String, album: String, start: Long, end: Long): Flow<Int>
    fun getAlbumTimePlayed(artist: String, album: String, start: Long, end: Long): Flow<Long>
    fun getMostPlayedAlbums(start: Long, end: Long): PagingSource<Int, AlbumInfo>
    fun getMostPlayedAlbumsByPlayCount(start: Long, end: Long): PagingSource<Int, AlbumInfo>
    fun getMostPlayedAlbumsFromArtist(artist: String, start: Long, end: Long): PagingSource<Int, AlbumInfo>
    fun getMostPlayedAlbumsFromArtistByPlayCount(artist: String, start: Long, end: Long): PagingSource<Int, AlbumInfo>
}


class RoomRepository(private val db: AppDatabase) : Repository {
    override suspend fun insertEvent(event: Event) = db.eventsDao().insert(event)
    override suspend fun deleteEvent(event: Event) = db.eventsDao().delete(event)
    override suspend fun updateEvent(event: Event) = db.eventsDao().update(event)
    override suspend fun getLastEventFromPlayer(playerId: String) = db.eventsDao().getLastEventFromPlayer(playerId)
    override suspend fun getEventsFromPlayer(playerId: String) = db.eventsDao().getEventsFromPlayer(playerId)
    override suspend fun deleteEventsFromPlayer(playerId: String) = db.eventsDao().deleteEventsFromPlayer(playerId)
    override suspend fun getEvents(eventIds: List<String>) = db.eventsDao().getEvents(eventIds)

    override suspend fun insertPlay(play: Play) = db.playDao().insert(play)
    override suspend fun deletePlay(play: Play) = db.playDao().delete(play)
    override suspend fun updatePlay(play: Play) = db.playDao().update(play)
    override suspend fun getLastPlayFromPlayer(playerId: String) = db.playDao().getLastPlayFromPlayer(playerId)
    override suspend fun clearPlaysFromPlayer(playerId: String) = db.playDao().clearPlaysFromPlayer(playerId)
    override fun getNowPlayingTracks(): Flow<List<Play>> = db.playDao().getNowPlayingTracks()
    override fun getRecentPlays() = db.playDao().getRecentPlays()

    override fun getArtistPlays(artist: String, start: Long, end: Long) = db.playDao().getArtistPlays(artist, start, end)
    override fun getArtistTimePlayed(artist: String, start: Long, end: Long) = db.playDao().getArtistTimePlayed(artist, start, end)
    override fun getMostPlayedArtists(start: Long, end: Long) = db.playDao().getMostPlayedArtists(start, end)
    override fun getMostPlayedArtistsByPlayCount(start: Long, end: Long) = db.playDao().getMostPlayedArtistsByPlayCount(start, end)
    override fun getArtistRank(time: Long, start: Long, end: Long) = db.playDao().getArtistRank(time, start, end)
    override fun getArtistRankByPlayCount(count: Int, start: Long, end: Long) = db.playDao().getArtistRankByPlayCount(count, start, end)

    override fun getAlbumPlays(artist: String, album: String, start: Long, end: Long) = db.playDao().getAlbumPlays(artist, album, start, end)
    override fun getAlbumTimePlayed(artist: String, album: String, start: Long, end: Long) = db.playDao().getAlbumTimePlayed(artist, album, start, end)
    override fun getMostPlayedAlbums(start: Long, end: Long) = db.playDao().getMostPlayedAlbums(start, end)
    override fun getMostPlayedAlbumsByPlayCount(start: Long, end: Long) = db.playDao().getMostPlayedAlbumsByPlayCount(start, end,)
    override fun getMostPlayedAlbumsFromArtist(artist: String, start: Long, end: Long) = db.playDao().getMostPlayedAlbumsFromArtist(artist, start, end)
    override fun getMostPlayedAlbumsFromArtistByPlayCount(artist: String, start: Long, end: Long) = db.playDao().getMostPlayedAlbumsFromArtistByPlayCount(artist, start, end)

    override fun getTrackPlays(artist: String, track: String, album: String?, start: Long, end: Long) = db.playDao().getTrackPlays(artist, track, album, start, end)
    override fun getTrackTimePlayed(artist: String, track: String, album: String?, start: Long, end: Long) = db.playDao().getTrackTimePlayed(artist, track, album, start, end)
    override fun getTrackHistory(artist: String, track: String, album: String?) = db.playDao().getTrackHistory(artist, track, album)
    override fun getMostPlayedTracks(start: Long, end: Long) = db.playDao().getMostPlayedTracks(start, end)
    override fun getMostPlayedTracksByPlayCount(start: Long, end: Long) = db.playDao().getMostPlayedTracksByPlayCount(start, end)
    override fun getMostPlayedTracksFromArtist(artist: String, start: Long, end: Long) = db.playDao().getMostPlayedTracksFromArtist(artist, start, end)
    override fun getMostPlayedTracksFromArtistByPlayCount(artist: String, start: Long, end: Long) = db.playDao().getMostPlayedTracksFromArtistByPlayCount(artist, start, end)
    override fun getMostPlayedTracksFromAlbum(artist: String, album: String, start: Long, end: Long) = db.playDao().getMostPlayedTracksFromAlbum(artist, album, start, end)
    override fun getMostPlayedTracksFromAlbumByPlayCount(artist: String, album: String, start: Long, end: Long) = db.playDao().getMostPlayedTracksFromAlbumByPlayCount(artist, album, start, end)
}
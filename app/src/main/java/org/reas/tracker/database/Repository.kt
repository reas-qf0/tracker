package org.reas.tracker.database

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
    fun getRecentPlays(amount: Int): Flow<List<Play>>

    fun getArtistPlays(artist: String): Flow<Int>
    fun getArtistTimePlayed(artist: String): Flow<Long>
    fun getMostPlayedArtists(start: Long, end: Long, amount: Int): Flow<List<String>>

    fun getTrackPlays(artist: String, track: String): Flow<Int>
    fun getTrackTimePlayed(artist: String, track: String): Flow<Long>
    fun getMostPlayedTracks(start: Long, end: Long, amount: Int): Flow<List<TrackInfo>>

    fun getAlbumPlays(artist: String, album: String): Flow<Int>
    fun getAlbumTimePlayed(artist: String, album: String): Flow<Long>
    fun getMostPlayedAlbums(start: Long, end: Long, amount: Int): Flow<List<AlbumInfo>>
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
    override fun getRecentPlays(amount: Int) = db.playDao().getRecentPlays(amount)

    override fun getArtistPlays(artist: String) = db.playDao().getArtistPlays(artist)
    override fun getArtistTimePlayed(artist: String) = db.playDao().getArtistTimePlayed(artist)
    override fun getMostPlayedArtists(start: Long, end: Long, amount: Int) = db.playDao().getMostPlayedArtists(start, end, amount)

    override fun getAlbumPlays(artist: String, album: String) = db.playDao().getAlbumPlays(artist, album)
    override fun getAlbumTimePlayed(artist: String, album: String) = db.playDao().getAlbumTimePlayed(artist, album)
    override fun getMostPlayedTracks(start: Long, end: Long, amount: Int) = db.playDao().getMostPlayedTracks(start, end, amount)

    override fun getTrackPlays(artist: String, track: String) = db.playDao().getTrackPlays(artist, track)
    override fun getTrackTimePlayed(artist: String, track: String) = db.playDao().getTrackTimePlayed(artist, track)
    override fun getMostPlayedAlbums(start: Long, end: Long, amount: Int) = db.playDao().getMostPlayedAlbums(start, end, amount)
}
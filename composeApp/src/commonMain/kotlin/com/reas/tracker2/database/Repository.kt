package com.reas.tracker2.database

import androidx.paging.PagingSource
import com.reas.tracker2.database.entities.ApiKeyEntity
import com.reas.tracker2.database.entities.EventEntity.Companion.toEntity
import com.reas.tracker2.database.entities.PlayEntity
import com.reas.tracker2.database.entities.PlayEntity.Companion.toEntity
import com.reas.tracker2.database.entities.ProcessingQueueEntity
import com.reas.tracker2.database.entities.SyncQueueEntity
import com.reas.tracker2.network.SyncEvent
import com.reas.tracker2.shared.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration
import kotlin.time.Instant

interface Repository {
    suspend fun insertEvent(event: Event): Long
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(event: Event)
    fun getEvents(): Flow<List<Event>>
    suspend fun insertEventInQueue(event: Event): Long
    suspend fun updateEventInQueue(event: Event)
    suspend fun deleteEventInQueue(event: Event)
    fun getEventsInQueue(): Flow<List<Event>>
    suspend fun clearQueue(app: String, timestamp: Instant)
    suspend fun insertEventInSync(event: Event): Long
    suspend fun updateEventInSync(event: Event)
    suspend fun deleteEventInSync(event: Event)
    fun getEventsInSync(): Flow<List<SyncEvent>>
    suspend fun deleteFromSync(ids: List<Long>)

    suspend fun insertPlay(play: Play): Long
    suspend fun insertPlays(plays: List<Play>)
    suspend fun updatePlay(play: Play)
    suspend fun deletePlay(play: Play)
    suspend fun getLastPlayFromSource(source: Source): Play?
    fun getNowPlayingTracks(): Flow<List<Play>>
    fun getRecentPlays(): PagingSource<Int, PlayEntity>

    fun getArtistPlays(artist: String, period: TimePeriod): Flow<Int>
    fun getArtistTimePlayed(artist: String, period: TimePeriod): Flow<Duration>
    fun getMostPlayedArtists(period: TimePeriod): PagingSource<Int, ArtistWithTimePlayed>
    fun getMostPlayedArtistsByPlayCount(period: TimePeriod): PagingSource<Int, ArtistWithPlayCount>
    fun getArtistRank(time: Duration, period: TimePeriod): Flow<Int>
    fun getArtistRankByPlayCount(count: Int, period: TimePeriod) : Flow<Int>

    fun getTrackPlays(track: TrackWithAlbum, period: TimePeriod): Flow<Int>
    fun getTrackTimePlayed(track: TrackWithAlbum, period: TimePeriod): Flow<Duration>
    fun getTrackHistory(track: TrackWithAlbum): PagingSource<Int, PlayEntity>
    fun getMostPlayedTracks(period: TimePeriod): PagingSource<Int, TrackWithTimePlayed>
    fun getMostPlayedTracksByPlayCount(period: TimePeriod): PagingSource<Int, TrackWithPlayCount>
    fun getMostPlayedTracksFromArtist(artist: String, period: TimePeriod): PagingSource<Int, TrackWithTimePlayed>
    fun getMostPlayedTracksFromArtistByPlayCount(artist: String, period: TimePeriod): PagingSource<Int, TrackWithPlayCount>
    fun getMostPlayedTracksFromAlbum(album: Album, period: TimePeriod): PagingSource<Int, TrackWithTimePlayed>
    fun getMostPlayedTracksFromAlbumByPlayCount(album: Album, period: TimePeriod): PagingSource<Int, TrackWithPlayCount>
    fun getAlbumRank(time: Duration, period: TimePeriod): Flow<Int>
    fun getAlbumRankByPlayCount(count: Int, period: TimePeriod) : Flow<Int>

    fun getAlbumPlays(album: Album, period: TimePeriod): Flow<Int>
    fun getAlbumTimePlayed(album: Album, period: TimePeriod): Flow<Duration>
    fun getMostPlayedAlbums(period: TimePeriod): PagingSource<Int, AlbumWithTimePlayed>
    fun getMostPlayedAlbumsByPlayCount(period: TimePeriod): PagingSource<Int, AlbumWithPlayCount>
    fun getMostPlayedAlbumsFromArtist(artist: String, period: TimePeriod): PagingSource<Int, AlbumWithTimePlayed>
    fun getMostPlayedAlbumsFromArtistByPlayCount(artist: String, period: TimePeriod): PagingSource<Int, AlbumWithPlayCount>
    fun getTrackRank(time: Duration, period: TimePeriod): Flow<Int>
    fun getTrackRankByPlayCount(count: Int, period: TimePeriod) : Flow<Int>

    suspend fun addKey(hostname: String, port: Int, username: String, key: String)
    suspend fun deleteKey(hostname: String, port: Int, username: String)
    suspend fun getKey(hostname: String, port: Int, username: String): String?

    // functions for debug info
    fun getEventCount(): Flow<Int>
    fun getUnprocessedEventCount(): Flow<Int>
    fun getUnsyncedEventCount(): Flow<Int>
    fun getPlayCount(): Flow<Int>
}


class RoomRepository(private val db: AppDatabase) : Repository {
    override suspend fun insertEvent(event: Event) = db.eventDao().insert(event.toEntity())
    override suspend fun deleteEvent(event: Event) = db.eventDao().delete(event.toEntity())
    override suspend fun updateEvent(event: Event) = db.eventDao().update(event.toEntity())
    override fun getEvents() = db.eventDao().getEvents().map { it.map { it.toObject() } }

    override suspend fun insertEventInQueue(event: Event) = db.processingQueueDao().insert(ProcessingQueueEntity(event.toEntity()))
    override suspend fun deleteEventInQueue(event: Event) = db.processingQueueDao().delete(ProcessingQueueEntity(event.toEntity()))
    override suspend fun updateEventInQueue(event: Event) = db.processingQueueDao().update(ProcessingQueueEntity(event.toEntity()))
    override fun getEventsInQueue() = db.processingQueueDao().getEvents().map { it.map { it.event.toObject() } }
    override suspend fun clearQueue(app: String, timestamp: Instant) = db.processingQueueDao().clearQueue(app, timestamp)

    override suspend fun insertEventInSync(event: Event) = db.syncQueueDao().insert(SyncQueueEntity(event.toEntity()))
    override suspend fun deleteEventInSync(event: Event) = db.syncQueueDao().delete(SyncQueueEntity(event.toEntity()))
    override suspend fun updateEventInSync(event: Event) = db.syncQueueDao().update(SyncQueueEntity(event.toEntity()))
    override fun getEventsInSync() = db.syncQueueDao().getEvents().map { it.map { SyncEvent(it.id, it.event.toObject()) } }
    override suspend fun deleteFromSync(ids: List<Long>)  = db.syncQueueDao().deleteByIds(ids)

    override suspend fun insertPlay(play: Play) = db.playDao().insert(play.toEntity())
    override suspend fun insertPlays(plays: List<Play>) = db.playDao().insertBatch(plays.map { it.toEntity() })
    override suspend fun deletePlay(play: Play) = db.playDao().delete(play.toEntity())
    override suspend fun updatePlay(play: Play) = db.playDao().update(play.toEntity())
    override suspend fun getLastPlayFromSource(source: Source) = db.playDao().getLastPlayFromSource(source.device, source.app)?.toObject()
    override fun getNowPlayingTracks() = db.playDao().getNowPlayingTracks().map { it.map { it.toObject() } }
    override fun getRecentPlays() = db.playDao().getRecentPlays()

    override fun getArtistPlays(artist: String, period: TimePeriod) = db.playDao().getArtistPlays(artist, period.start, period.end)
    override fun getArtistTimePlayed(artist: String, period: TimePeriod) = db.playDao().getArtistTimePlayed(artist, period.start, period.end)
    override fun getMostPlayedArtists(period: TimePeriod) = db.playDao().getMostPlayedArtists(period.start, period.end)
    override fun getMostPlayedArtistsByPlayCount(period: TimePeriod) = db.playDao().getMostPlayedArtistsByPlayCount(period.start, period.end)
    override fun getArtistRank(time: Duration, period: TimePeriod) = db.playDao().getArtistRank(time, period.start, period.end)
    override fun getArtistRankByPlayCount(count: Int, period: TimePeriod) = db.playDao().getArtistRankByPlayCount(count, period.start, period.end)

    override fun getAlbumPlays(album: Album, period: TimePeriod) = db.playDao().getAlbumPlays(album.artist, album.title, period.start, period.end)
    override fun getAlbumTimePlayed(album: Album, period: TimePeriod) = db.playDao().getAlbumTimePlayed(album.artist, album.title, period.start, period.end)
    override fun getMostPlayedAlbums(period: TimePeriod) = db.playDao().getMostPlayedAlbums(period.start, period.end)
    override fun getMostPlayedAlbumsByPlayCount(period: TimePeriod) = db.playDao().getMostPlayedAlbumsByPlayCount(period.start, period.end)
    override fun getMostPlayedAlbumsFromArtist(artist: String, period: TimePeriod) = db.playDao().getMostPlayedAlbumsFromArtist(artist, period.start, period.end)
    override fun getMostPlayedAlbumsFromArtistByPlayCount(artist: String, period: TimePeriod) = db.playDao().getMostPlayedAlbumsFromArtistByPlayCount(artist, period.start, period.end)
    override fun getAlbumRank(time: Duration, period: TimePeriod) = db.playDao().getAlbumRank(time, period.start, period.end)
    override fun getAlbumRankByPlayCount(count: Int, period: TimePeriod) = db.playDao().getAlbumRankByPlayCount(count, period.start, period.end)

    override fun getTrackPlays(track: TrackWithAlbum, period: TimePeriod) = db.playDao().getTrackPlays(track.artist, track.track, track.album, track.albumArtist, period.start, period.end)
    override fun getTrackTimePlayed(track: TrackWithAlbum, period: TimePeriod) = db.playDao().getTrackTimePlayed(track.artist, track.track, track.album, track.albumArtist, period.start, period.end)
    override fun getTrackHistory(track: TrackWithAlbum) = db.playDao().getTrackHistory(track.artist, track.track, track.album, track.albumArtist)
    override fun getMostPlayedTracks(period: TimePeriod) = db.playDao().getMostPlayedTracks(period.start, period.end)
    override fun getMostPlayedTracksByPlayCount(period: TimePeriod) = db.playDao().getMostPlayedTracksByPlayCount(period.start, period.end)
    override fun getMostPlayedTracksFromArtist(artist: String, period: TimePeriod) = db.playDao().getMostPlayedTracksFromArtist(artist, period.start, period.end)
    override fun getMostPlayedTracksFromArtistByPlayCount(artist: String, period: TimePeriod) = db.playDao().getMostPlayedTracksFromArtistByPlayCount(artist, period.start, period.end)
    override fun getMostPlayedTracksFromAlbum(album: Album, period: TimePeriod) = db.playDao().getMostPlayedTracksFromAlbum(album.artist, album.title, period.start, period.end)
    override fun getMostPlayedTracksFromAlbumByPlayCount(album: Album, period: TimePeriod) = db.playDao().getMostPlayedTracksFromAlbumByPlayCount(album.artist, album.title, period.start, period.end)
    override fun getTrackRank(time: Duration, period: TimePeriod) = db.playDao().getTrackRank(time, period.start, period.end)
    override fun getTrackRankByPlayCount(count: Int, period: TimePeriod) = db.playDao().getTrackRankByPlayCount(count, period.start, period.end)

    override suspend fun addKey(hostname: String, port: Int, username: String, key: String) = db.apiKeyDao().insert(ApiKeyEntity(hostname, port, username, key))
    override suspend fun deleteKey(hostname: String, port: Int, username: String) = db.apiKeyDao().delete(hostname, port, username)
    override suspend fun getKey(hostname: String, port: Int, username: String) = db.apiKeyDao().getKey(hostname, port, username)

    override fun getEventCount() = db.eventDao().getEventsCount()
    override fun getUnprocessedEventCount() = db.processingQueueDao().getEventCount()
    override fun getUnsyncedEventCount() = db.syncQueueDao().getEventCount()
    override fun getPlayCount() = db.playDao().getPlayCount()
}
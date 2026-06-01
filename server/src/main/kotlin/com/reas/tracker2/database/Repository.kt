package com.reas.tracker2.database

import com.reas.tracker2.database.tables.*
import com.reas.tracker2.shared.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface Repository {
    fun getOrInsertArtists(artists: List<String>): List<Long>
    fun getOrInsertAlbum(name: String, artists: List<String>): Long
    fun getOrInsertTrack(name: String, artists: List<String>, album: String?, albumArtists: List<String>?): Long
    fun getArtists(ids: List<Long>): ArtistList
    fun getAlbum(id: Long): Album
    fun getTrack(id: Long): TrackWithAlbum

    fun insertEvents(events: List<Event>)
    fun insertPlays(plays: List<Play>)
    fun getEvents(): List<Event>
    fun getPlays(): List<Play>
    fun getLastPlayFromSource(source: Source): Play?

    fun registerUser(user: String): String
    fun getUser(key: String): String?
    fun getMissedPlays(apiKey: String): List<Play>
    fun getNextIds(): MutableMap<String, Long>
    fun setLastSeenId(apiKey: String, id: Long)
}

class DatabaseRepository(private val database: Database) : Repository {
    private fun getOrInsertArtistsNoTransaction(artists: List<String>): List<Long> {
        val existingArtists = ArtistTable.selectAll()
            .where(ArtistTable.name inList artists)
            .associate { it[ArtistTable.name] to it[ArtistTable.id].value }
        val newArtists = artists.filter { !existingArtists.containsKey(it) }
        return if (newArtists.isEmpty()) {
            artists.map { existingArtists[it]!! }
        } else {
            val newArtistIds = ArtistTable.batchInsert(newArtists) { artist ->
                this[ArtistTable.name] = artist
            }.associate { it[ArtistTable.name] to it[ArtistTable.id].value }
            artists.map { existingArtists[it] ?: newArtistIds[it]!! }
        }
    }
    override fun getOrInsertArtists(artists: List<String>) = transaction(database) {
        getOrInsertArtistsNoTransaction(artists)
    }

    private fun getOrInsertAlbumNoTransaction(name: String, artists: List<String>): Long {
        val artistIds = getOrInsertArtistsNoTransaction(artists)
        val artistIdsAsString = artistIds.joinToString(",")

        val albumId = AlbumTable.select(AlbumTable.id)
            .where { (AlbumTable.name eq name) and (AlbumTable.artistIds eq artistIdsAsString) }
            .firstOrNull()?.get(AlbumTable.id)?.value

        return albumId ?: run {
            val newAlbumId = AlbumTable.insert {
                it[AlbumTable.name] = name
                it[AlbumTable.artistIds] = artistIdsAsString
            }[AlbumTable.id].value
            AlbumArtistCrossRefTable.batchInsert(artistIds) { artistId ->
                this[AlbumArtistCrossRefTable.albumId] = newAlbumId
                this[AlbumArtistCrossRefTable.artistId] = artistId
            }
            newAlbumId
        }
    }

    override fun getOrInsertAlbum(name: String, artists: List<String>) = transaction(database) {
        getOrInsertAlbumNoTransaction(name, artists)
    }

    private fun getOrInsertTrackNoTransaction(name: String, artists: List<String>, album: String?, albumArtists: List<String>?): Long {
        val artistIds = getOrInsertArtistsNoTransaction(artists)
        val artistIdsAsString = artistIds.joinToString(",")
        val albumId = album?.let { getOrInsertAlbumNoTransaction(album, albumArtists!!) }

        val trackId = TrackTable.select(TrackTable.id)
            .where {
                (TrackTable.name eq name) and
                        (TrackTable.artistIds eq artistIdsAsString) and
                        (TrackTable.albumId eq albumId)
            }.firstOrNull()?.get(TrackTable.id)?.value

        return trackId ?: run {
            val newTrackId = TrackTable.insert {
                it[TrackTable.name] = name
                it[TrackTable.albumId] = albumId
                it[TrackTable.artistIds] = artistIdsAsString
            }[TrackTable.id].value
            TrackArtistCrossRefTable.batchInsert(artistIds) { artistId ->
                this[TrackArtistCrossRefTable.artistId] = artistId
                this[TrackArtistCrossRefTable.trackId] = newTrackId
            }
            newTrackId
        }
    }

    override fun getOrInsertTrack(name: String, artists: List<String>, album: String?, albumArtists: List<String>?) = transaction(database) {
        getOrInsertTrackNoTransaction(name, artists, album, albumArtists)
    }

    private fun getOrInsertTrack(track: TrackWithAlbum) = getOrInsertTrackNoTransaction(
        track.name,
        track.artists.map { it.name },
        track.asAlbum?.name,
        track.asAlbum?.artists?.map { it.name },
    )

    override fun getArtists(ids: List<Long>) = transaction(database) {
        val artists = ArtistTable.selectAll()
            .where(ArtistTable.id inList ids)
            .map { Artist(it[ArtistTable.name], it[ArtistTable.id].value) }
        ArtistList(artists, artists.joinToString(", ") { it.name })
    }

    override fun getAlbum(id: Long) = transaction(database) {
        AlbumTable.selectAll()
            .where(AlbumTable.id eq id)
            .first().let {
                Album(
                    it[AlbumTable.name],
                    getArtists(it[AlbumTable.artistIds].split(",").map { it.toLong() }),
                    it[AlbumTable.id].value
                )
            }
    }

    override fun getTrack(id: Long) = transaction(database) {
        TrackTable.selectAll()
            .where(TrackTable.id eq id)
            .first().let {
                TrackWithAlbum(
                    trackObject = Track(
                        it[TrackTable.name],
                        getArtists(it[TrackTable.artistIds].split(",").map { it.toLong() }),
                        it[TrackTable.id].value
                    ),
                    albumObject = it[TrackTable.albumId]?.let { getAlbum(it.value) }
                )
            }
    }

    override fun insertEvents(events: List<Event>) {
        transaction(database) {
            EventTable.batchUpsert(events, shouldReturnGeneratedValues = false) { event ->
                this[EventTable.trackId] = getOrInsertTrack(event.metadata)
                this[EventTable.timestamp] = event.timestamp.toEpochMilliseconds()
                this[EventTable.position] = event.position.inWholeMilliseconds
                this[EventTable.duration] = event.duration.inWholeMilliseconds
                this[EventTable.state] = event.state.name
                this[EventTable.sourceUser] = event.user
                this[EventTable.sourceDevice] = event.client
                this[EventTable.sourceApp] = event.app
            }
        }
    }

    override fun getNextIds() = transaction(database) {
        val maxId = PlayTable.id.max()
        PlayTable.select(maxId, PlayTable.sourceUser)
            .groupBy(PlayTable.sourceUser)
            .toList()
            .associate { it[PlayTable.sourceUser] to (it[maxId] ?: 0) }
            .toMutableMap()
    }

    override fun insertPlays(plays: List<Play>) {
        transaction(database) {
            PlayTable.batchUpsert(plays, shouldReturnGeneratedValues = false) { play ->
                this[PlayTable.trackId] = getOrInsertTrack(play.metadata)
                this[PlayTable.timestamp] = play.timestamp.toEpochMilliseconds()
                this[PlayTable.timePlayed] = play.timePlayed.inWholeMilliseconds
                this[PlayTable.duration] = play.duration.inWholeMilliseconds
                this[PlayTable.lastPosition] = play.lastPosition.inWholeMilliseconds
                this[PlayTable.lastPlaying] = play.lastPlaying
                this[PlayTable.sourceUser] = play.user
                this[PlayTable.sourceDevice] = play.client
                this[PlayTable.sourceApp] = play.app
                this[PlayTable.associatedEvents] = Json.encodeToString(play.associatedEvents)
                this[PlayTable.id] = play.id!!
            }
        }
    }

    override fun getEvents(): List<Event> = transaction(database) {
        EventTable.selectAll()
            .orderBy(EventTable.timestamp, SortOrder.DESC)
            .map { it.toEvent() }
    }

    override fun getPlays(): List<Play> = transaction(database) {
        PlayTable.selectAll()
            .orderBy(PlayTable.timestamp, SortOrder.DESC)
            .map { it.toPlay() }
    }

    override fun getLastPlayFromSource(source: Source): Play? = transaction(database) {
        PlayTable.selectAll()
            .where {
                (PlayTable.sourceApp eq source.app) and
                (PlayTable.sourceDevice eq source.client) and
                (PlayTable.sourceUser eq source.user)
            }
            .orderBy(PlayTable.timestamp, SortOrder.DESC)
            .limit(1)
            .firstOrNull()?.toPlay()
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun registerUser(user: String) = transaction(database) {
        val key = Uuid.random().toHexString()
        ApiKeyTable.insert {
            it[ApiKeyTable.user] = user
            it[ApiKeyTable.key] = key
            it[ApiKeyTable.lastSeenId] = 0
        }
        return@transaction key
    }

    override fun getUser(key: String) = transaction(database) {
        ApiKeyTable.selectAll()
            .where { ApiKeyTable.key eq key }
            .limit(1).firstOrNull()?.get(ApiKeyTable.user)
    }

    override fun getMissedPlays(apiKey: String) = transaction(database) {
        val apiKeyInfo = ApiKeyTable.selectAll()
            .where { ApiKeyTable.key eq apiKey }.first()
        val user = apiKeyInfo[ApiKeyTable.user]
        val lastSeen = apiKeyInfo[ApiKeyTable.lastSeenId]

        PlayTable.selectAll().where {
            (PlayTable.sourceUser eq user) and (PlayTable.id greaterEq lastSeen)
        }.orderBy(PlayTable.id).map { it.toPlay() }
    }

    override fun setLastSeenId(apiKey: String, id: Long) {
        transaction(database) {
            ApiKeyTable.update({ ApiKeyTable.key eq apiKey }) {
                it[ApiKeyTable.lastSeenId] = id
            }
        }
    }

    private fun ResultRow.toEvent(): Event =
        Event(
            metadata = getTrack(this[EventTable.trackId].value),
            duration = this[EventTable.duration].milliseconds,
            info = EventInfo(
                position = this[EventTable.position].milliseconds,
                timestamp = Instant.fromEpochMilliseconds(this[EventTable.timestamp]),
                state = EventState.valueOf(this[EventTable.state])
            ),
            source = Source(
                user = this[EventTable.sourceUser],
                client = this[EventTable.sourceDevice],
                app = this[EventTable.sourceApp]
            )
        )

    private fun ResultRow.toPlay(): Play =
        Play(
            metadata = getTrack(this[PlayTable.trackId].value),
            timestamp = Instant.fromEpochMilliseconds(this[PlayTable.timestamp]),
            duration = this[PlayTable.duration].milliseconds,
            timePlayed = this[PlayTable.timePlayed].milliseconds,
            source = Source(
                user = this[PlayTable.sourceUser],
                client = this[PlayTable.sourceDevice],
                app = this[PlayTable.sourceApp]
            ),
            associatedEvents = Json.decodeFromString(this[PlayTable.associatedEvents]),
            id = this[PlayTable.id]
        )
}
package com.reas.tracker2.database

import com.reas.tracker2.database.tables.ApiKeyTable
import com.reas.tracker2.database.tables.EventTable
import com.reas.tracker2.database.tables.PlayTable
import com.reas.tracker2.shared.Event
import com.reas.tracker2.shared.Play
import com.reas.tracker2.shared.Source
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface Repository {
    fun insertEvents(events: List<Event>)
    fun insertPlays(plays: List<Play>)
    fun getEvents(): List<Event>
    fun getPlays(): List<Play>
    fun getLastPlayFromSource(source: Source): Play?

    fun registerUser(user: String): String
    fun getUser(key: String): String?
    fun getMissedPlays(user: String, lastSeen: Long): List<Play>
    fun getNextIds(): MutableMap<String, Long>
}

class DatabaseRepository(private val database: Database) : Repository {
    override fun insertEvents(events: List<Event>) {
        transaction(database, readOnly = false) {
            EventTable.batchUpsert(events, shouldReturnGeneratedValues = false) { event ->
                this[EventTable.track] = event.track
                this[EventTable.artist] = event.artist
                this[EventTable.album] = event.album
                this[EventTable.albumArtist] = event.albumArtist
                this[EventTable.timestamp] = event.timestamp.toEpochMilliseconds()
                this[EventTable.position] = event.position.inWholeMilliseconds
                this[EventTable.duration] = event.duration.inWholeMilliseconds
                this[EventTable.isPlaying] = event.isPlaying
                this[EventTable.sourceUser] = event.user
                this[EventTable.sourceDevice] = event.device
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
        transaction(database, readOnly = false) {
            PlayTable.batchUpsert(plays, shouldReturnGeneratedValues = false) { play ->
                this[PlayTable.track] = play.track
                this[PlayTable.artist] = play.artist
                this[PlayTable.album] = play.album
                this[PlayTable.albumArtist] = play.albumArtist
                this[PlayTable.timestamp] = play.timestamp.toEpochMilliseconds()
                this[PlayTable.timePlayed] = play.timePlayed.inWholeMilliseconds
                this[PlayTable.duration] = play.duration.inWholeMilliseconds
                this[PlayTable.lastPosition] = play.lastPosition.inWholeMilliseconds
                this[PlayTable.lastPlaying] = play.lastPlaying
                this[PlayTable.sourceUser] = play.sourceUser
                this[PlayTable.sourceDevice] = play.sourceDevice
                this[PlayTable.sourceApp] = play.sourceApp
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
                (PlayTable.sourceDevice eq source.device) and
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
        }
        return@transaction key
    }

    override fun getUser(key: String) = transaction(database) {
        ApiKeyTable.selectAll()
            .where { ApiKeyTable.key eq key }
            .limit(1).firstOrNull()?.get(ApiKeyTable.user)
    }

    override fun getMissedPlays(user: String, lastSeen: Long) = transaction(database) {
        PlayTable.selectAll().where {
            (PlayTable.sourceUser eq user) and (PlayTable.id greaterEq lastSeen)
        }.orderBy(PlayTable.id).map { it.toPlay() }
    }

    private fun ResultRow.toEvent(): Event =
        Event.create(
            track = this[EventTable.track],
            artist = this[EventTable.artist],
            album = this[EventTable.album],
            albumArtist = this[EventTable.albumArtist],
            timestamp = this[EventTable.timestamp],
            position = this[EventTable.position],
            duration = this[EventTable.duration],
            isPlaying = this[EventTable.isPlaying],
            source = Source(
                user = this[EventTable.sourceUser],
                device = this[EventTable.sourceDevice],
                app = this[EventTable.sourceApp]
            )
        )

    private fun ResultRow.toPlay(): Play =
        Play.create(
            track = this[PlayTable.track],
            artist = this[PlayTable.artist],
            album = this[PlayTable.album],
            albumArtist = this[PlayTable.albumArtist],
            timestamp = this[PlayTable.timestamp],
            duration = this[PlayTable.duration],
            timePlayed = this[PlayTable.timePlayed],
            source = Source(
                user = this[PlayTable.sourceUser],
                device = this[PlayTable.sourceDevice],
                app = this[PlayTable.sourceApp]
            ),
            associatedEvents = Json.decodeFromString(this[PlayTable.associatedEvents]),
            id = this[PlayTable.id]
        )
}
package com.reas.tracker2.database

import com.reas.tracker2.shared.Event
import com.reas.tracker2.shared.Play
import com.reas.tracker2.shared.Source
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert

interface Repository {
    fun insertEvent(event: Event)
    fun insertPlay(play: Play)
    fun getEvents(): List<Event>
    fun getPlays(): List<Play>
    fun getLastPlayFromSource(source: Source): Play?
}

class DatabaseRepository(private val database: Database) : Repository {
    override fun insertEvent(event: Event) {
        transaction(database) {
            EventTable.upsert {
                it[EventTable.track] = event.track
                it[EventTable.artist] = event.artist
                it[EventTable.album] = event.album
                it[EventTable.albumArtist] = event.albumArtist
                it[EventTable.timestamp] = event.timestamp.toEpochMilliseconds()
                it[EventTable.position] = event.position.inWholeMilliseconds
                it[EventTable.duration] = event.duration.inWholeMilliseconds
                it[EventTable.isPlaying] = event.isPlaying
                it[EventTable.sourceUser] = event.user
                it[EventTable.sourceDevice] = event.device
                it[EventTable.sourceApp] = event.app
            }
        }
    }

    override fun insertPlay(play: Play) {
        transaction(database) {
            PlayTable.upsert {
                it[PlayTable.track] = play.track
                it[PlayTable.artist] = play.artist
                it[PlayTable.album] = play.album
                it[PlayTable.albumArtist] = play.albumArtist
                it[PlayTable.timestamp] = play.timestamp.toEpochMilliseconds()
                it[PlayTable.timePlayed] = play.timePlayed.inWholeMilliseconds
                it[PlayTable.duration] = play.duration.inWholeMilliseconds
                it[PlayTable.lastPosition] = play.lastPosition.inWholeMilliseconds
                it[PlayTable.lastPlaying] = play.lastPlaying
                it[PlayTable.sourceUser] = play.sourceUser
                it[PlayTable.sourceDevice] = play.sourceDevice
                it[PlayTable.sourceApp] = play.sourceApp
                it[PlayTable.associatedEvents] = Json.encodeToString(play.associatedEvents)
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
            associatedEvents = Json.decodeFromString(this[PlayTable.associatedEvents])
        )
}
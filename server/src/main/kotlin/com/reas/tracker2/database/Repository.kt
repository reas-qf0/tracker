package com.reas.tracker2.database

import com.reas.tracker2.shared.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

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
                it[EventTable.sourceUser] = event.sourceUser
                it[EventTable.sourceDevice] = event.sourceDevice
                it[EventTable.sourceApp] = event.sourceApp
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
        Event(
            metadata = Metadata(
                duration = this[EventTable.duration].milliseconds,
                info = TrackWithOptionalAlbum(
                    _track = Track(
                        title = this[EventTable.track],
                        artist = this[EventTable.artist],
                    ),
                    _album = if (this[EventTable.album] != null) Album(
                        title = this[EventTable.album]!!,
                        artist = this[EventTable.albumArtist]!!
                    ) else null
                )
            ),
            timestamp = Instant.fromEpochMilliseconds(this[EventTable.timestamp]),
            position = this[EventTable.position].milliseconds,
            isPlaying = this[EventTable.isPlaying],
            source = Source(
                user = this[EventTable.sourceUser],
                device = this[EventTable.sourceDevice],
                app = this[EventTable.sourceApp]
            )
        )

    private fun ResultRow.toPlay(): Play =
        Play(
            metadata = Metadata(
                duration = this[PlayTable.duration].milliseconds,
                info = TrackWithOptionalAlbum(
                    _track = Track(
                        title = this[PlayTable.track],
                        artist = this[PlayTable.artist],
                    ),
                    _album = if (this[PlayTable.album] != null) Album(
                        title = this[PlayTable.album]!!,
                        artist = this[PlayTable.albumArtist]!!
                    ) else null
                )
            ),
            timestamp = Instant.fromEpochMilliseconds(this[PlayTable.timestamp]),
            timePlayed = this[PlayTable.timePlayed].milliseconds,
            source = Source(
                user = this[PlayTable.sourceUser],
                device = this[PlayTable.sourceDevice],
                app = this[PlayTable.sourceApp]
            ),
            associatedEvents = Json.decodeFromString(this[PlayTable.associatedEvents])
        )
}
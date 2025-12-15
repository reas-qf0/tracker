package org.reas.tracker.database

import kotlinx.coroutines.flow.Flow

interface Repository {
    suspend fun insertEvent(event: Event): Long
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(event: Event)
    fun getEventsLaterThan(timestamp: Long): Flow<List<Event>>

    suspend fun insertPlay(play: Play): Long
    suspend fun updatePlay(play: Play)
    suspend fun deletePlay(play: Play)
    fun getRecentPlays(amount: Int): Flow<List<Play>>
}


class RoomRepository(private val db: AppDatabase) : Repository {
    override suspend fun insertEvent(event: Event) = db.eventsDao().insert(event)
    override suspend fun deleteEvent(event: Event) = db.eventsDao().delete(event)
    override suspend fun updateEvent(event: Event) = db.eventsDao().update(event)
    override fun getEventsLaterThan(timestamp: Long) = db.eventsDao().getEventsLaterThan(timestamp)

    override suspend fun insertPlay(play: Play) = db.playDao().insert(play)
    override suspend fun deletePlay(play: Play) = db.playDao().delete(play)
    override suspend fun updatePlay(play: Play) = db.playDao().update(play)
    override fun getRecentPlays(amount: Int) = db.playDao().getRecentPlays(amount)
}
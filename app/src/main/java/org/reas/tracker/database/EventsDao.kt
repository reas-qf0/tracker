package org.reas.tracker.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Update

@Dao
interface EventsDao {
    @Insert(onConflict = REPLACE)
    suspend fun insert(event: Event): Long

    @Update
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)

    @Query("SELECT * FROM events WHERE id IN(:eventIds)")
    suspend fun getEvents(eventIds: List<String>): List<Event>

    @Query("SELECT * FROM events WHERE playerId = :playerId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastEventFromPlayer(playerId: String): Event?

    @Query("SELECT * FROM events WHERE playerId = :playerId")
    suspend fun getEventsFromPlayer(playerId: String): List<Event>
}
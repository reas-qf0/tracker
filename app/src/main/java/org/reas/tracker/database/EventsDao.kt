package org.reas.tracker.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventsDao {
    @Insert
    suspend fun insert(event: Event): Long

    @Update
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)

    @Query("SELECT * FROM events WHERE timestamp >= :timestamp ORDER BY timestamp")
    fun getEventsLaterThan(timestamp: Long): Flow<List<Event>>
}
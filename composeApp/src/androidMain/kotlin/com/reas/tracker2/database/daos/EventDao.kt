package com.reas.tracker2.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.reas.tracker2.database.objects.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert(onConflict = REPLACE)
    suspend fun insert(event: Event): Long

    @Update
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)

    @Query("SELECT * FROM events")
    fun getEvents(): Flow<List<Event>>

    @Query("DELETE FROM events WHERE sourceApp = :app AND timestamp <= :timestamp")
    suspend fun clearQueue(app: String, timestamp: Long)
}
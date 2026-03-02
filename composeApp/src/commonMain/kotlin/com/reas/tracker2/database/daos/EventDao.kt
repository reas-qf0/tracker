package com.reas.tracker2.database.daos

import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.REPLACE
import com.reas.tracker2.database.entities.EventEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

@Dao
interface EventDao {
    @Insert(onConflict = REPLACE)
    suspend fun insert(event: EventEntity): Long

    @Update
    suspend fun update(event: EventEntity)

    @Delete
    suspend fun delete(event: EventEntity)

    @Query("SELECT * FROM events")
    fun getEvents(): Flow<List<EventEntity>>

    @Query("DELETE FROM events WHERE sourceApp = :app AND timestamp <= :timestamp")
    suspend fun clearQueue(app: String, timestamp: Instant)
}
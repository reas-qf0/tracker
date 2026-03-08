package com.reas.tracker2.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.reas.tracker2.database.entities.ProcessingQueueEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant


@Dao
interface ProcessingQueueDao {
    @Insert(onConflict = REPLACE)
    suspend fun insert(event: ProcessingQueueEntity): Long

    @Update
    suspend fun update(event: ProcessingQueueEntity)

    @Delete
    suspend fun delete(event: ProcessingQueueEntity)

    @Query("SELECT * FROM processing_queue")
    fun getEvents(): Flow<List<ProcessingQueueEntity>>

    @Query("DELETE FROM processing_queue WHERE sourceApp = :app AND timestamp <= :timestamp")
    suspend fun clearQueue(app: String, timestamp: Instant)
}
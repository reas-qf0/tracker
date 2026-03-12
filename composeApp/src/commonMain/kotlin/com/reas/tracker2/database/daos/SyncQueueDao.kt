package com.reas.tracker2.database.daos

import androidx.room.*
import com.reas.tracker2.database.entities.SyncQueueEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface SyncQueueDao {
    @Insert
    suspend fun insert(event: SyncQueueEntity): Long

    @Update
    suspend fun update(event: SyncQueueEntity)

    @Delete
    suspend fun delete(event: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue")
    fun getEvents(): Flow<List<SyncQueueEntity>>

    @Query("DELETE FROM sync_queue WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
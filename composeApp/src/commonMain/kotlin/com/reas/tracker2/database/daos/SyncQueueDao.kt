package com.reas.tracker2.database.daos

import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.REPLACE
import com.reas.tracker2.database.entities.SyncQueueEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface SyncQueueDao {
    @Insert(onConflict = REPLACE)
    suspend fun insert(event: SyncQueueEntity): Long

    @Update
    suspend fun update(event: SyncQueueEntity)

    @Delete
    suspend fun delete(event: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue")
    fun getEvents(): Flow<List<SyncQueueEntity>>

    @Query("DELETE FROM sync_queue")
    suspend fun clearSyncQueue()
}
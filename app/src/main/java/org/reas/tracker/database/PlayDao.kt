package org.reas.tracker.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayDao {
    @Insert
    suspend fun insert(play: Play): Long

    @Update
    suspend fun update(play: Play)

    @Delete
    suspend fun delete(play: Play)

    @Query("SELECT * FROM plays ORDER BY timestamp DESC LIMIT :amount")
    fun getRecentPlays(amount: Int): Flow<List<Play>>
}
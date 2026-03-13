package com.reas.tracker2.database.daos

import androidx.room.*
import com.reas.tracker2.database.entities.ApiKeyEntity

@Dao
interface ApiKeyDao {
    @Insert
    suspend fun insert(key: ApiKeyEntity)

    @Update
    suspend fun update(key: ApiKeyEntity)

    @Delete
    suspend fun delete(key: ApiKeyEntity)

    @Query("SELECT `key` FROM api_keys WHERE hostname = :hostname AND port = :port AND username = :username")
    suspend fun getKey(hostname: String, port: Int, username: String): String?
}
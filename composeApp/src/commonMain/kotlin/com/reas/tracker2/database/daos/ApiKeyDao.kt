package com.reas.tracker2.database.daos

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.reas.tracker2.database.entities.ApiKeyEntity

@Dao
interface ApiKeyDao {
    @Insert
    suspend fun insert(key: ApiKeyEntity)

    @Update
    suspend fun update(key: ApiKeyEntity)

    @Query("SELECT `key` FROM api_keys WHERE hostname = :hostname AND port = :port AND username = :username")
    suspend fun getKey(hostname: String, port: Int, username: String): String?

    @Query("DELETE FROM api_keys WHERE hostname = :hostname AND port = :port AND username = :username")
    suspend fun delete(hostname: String, port: Int, username: String)
}
package com.reas.tracker2.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_keys")
data class ApiKeyEntity(
    val hostname: String,
    val port: Int,
    val username: String,
    @PrimaryKey val key: String
)
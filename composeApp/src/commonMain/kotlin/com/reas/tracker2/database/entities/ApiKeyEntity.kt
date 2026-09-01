package com.reas.tracker2.database.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "api_keys")
data class ApiKeyEntity(
    val hostname: String,
    val port: Int,
    val username: String,
    @PrimaryKey val key: String
)
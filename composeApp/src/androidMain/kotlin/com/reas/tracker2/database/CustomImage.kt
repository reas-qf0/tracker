package com.reas.tracker2.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_images")
data class CustomImage(
    @PrimaryKey val arguments: List<String>,
    val filename: String
)
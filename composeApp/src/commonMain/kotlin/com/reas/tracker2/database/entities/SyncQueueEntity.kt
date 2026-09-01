package com.reas.tracker2.database.entities

import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @Embedded
    val event: EventEntity,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
)
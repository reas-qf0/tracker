package com.reas.tracker2.database.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "processing_queue")
data class ProcessingQueueEntity(
    @Embedded
    val event: EventEntity,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
)
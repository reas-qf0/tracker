package com.reas.tracker2

import kotlinx.serialization.Serializable

@Serializable
data class EventRepr(
    val track: String,
    val artist: String,
    val album: String?,
    val album_artist: String?,
    val timestamp: Long,
    val position: Long,
    val duration: Long,
    val is_playing: Int,
    val source_user: String,
    val source_device: String,
    val source_app: String,
)
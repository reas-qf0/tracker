package org.reas.tracker.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TrackHistoryScreen(
    artist: String,
    track: String,
    modifier: Modifier = Modifier,
    album: String? = null
) {
    Text("""
        TrackHistoryScreen
        
        artist=$artist
        track=$track
        album=$album
    """.trimIndent())
}
package org.reas.tracker.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TrackInfoScreen(
    artist: String,
    track: String,
    modifier: Modifier = Modifier,
    album: String? = null
) {
    Text("""
        TrackInfoScreen
        
        artist=$artist
        track=$track
        album=$album
    """.trimIndent())
}
package org.reas.tracker.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AlbumInfoScreen(
    artist: String,
    album: String,
    modifier: Modifier = Modifier
) {
    Text("""
        AlbumInfoScreen
        
        artist=$artist
        album=$album
    """.trimIndent())
}
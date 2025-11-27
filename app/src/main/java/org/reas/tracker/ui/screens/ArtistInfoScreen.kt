package org.reas.tracker.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ArtistInfoScreen(
    artist: String,
    modifier: Modifier = Modifier
) {
    Text("""
        ArtistInfoScreen
        
        artist=$artist
    """.trimIndent())
}
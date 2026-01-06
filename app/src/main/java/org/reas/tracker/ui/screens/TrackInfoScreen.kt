package org.reas.tracker.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.reas.tracker.ui.navigation.TrackInfo

@Composable
fun TrackInfoScreen(
    arguments: TrackInfo,
    modifier: Modifier = Modifier,
) {
    Text("""
        TrackInfoScreen
        
        artist=${arguments.artist}
        track=${arguments.track}
        album=${arguments.album}
    """.trimIndent())
}
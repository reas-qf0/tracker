package org.reas.tracker.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.reas.tracker.ui.navigation.AlbumInfo

@Composable
fun AlbumInfoScreen(
    arguments: AlbumInfo,
    modifier: Modifier = Modifier
) {
    Text("""
        AlbumInfoScreen
        
        artist=${arguments.artist}
        album=${arguments.album}
    """.trimIndent())
}
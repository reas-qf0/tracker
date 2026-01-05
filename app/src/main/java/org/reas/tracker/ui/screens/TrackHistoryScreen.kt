package org.reas.tracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import org.reas.tracker.ui.components.BottomSheetInfo
import org.reas.tracker.ui.components.HistoryEntry
import org.reas.tracker.ui.components.InfoBottomSheet
import org.reas.tracker.ui.components.showAlbumAndTrack
import org.reas.tracker.ui.components.showTrack
import org.reas.tracker.ui.viewmodels.TrackHistoryViewModel
import org.reas.tracker.ui.viewmodels.ViewModelProvider

@Composable
fun TrackHistoryScreen(
    artist: String,
    track: String,
    modifier: Modifier = Modifier,
    album: String? = null,
    navigateToArtist: (String) -> Unit,
    navigateToAlbum: (String, String) -> Unit,
    navigateToTrack: (String, String, String?) -> Unit,
    navigateToTrackHistory: (String, String, String?) -> Unit,
    viewModel: TrackHistoryViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val trackPlays by remember { viewModel.trackPlays(artist, track, album) }.collectAsState()
    val history = remember { viewModel.history(artist, track, album) }.collectAsLazyPagingItems()
    val bottomSheetState = remember { mutableStateOf<BottomSheetInfo?>(null) }

    Column {
        Text(
            "Plays: $trackPlays",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
        )

        LazyColumn(modifier = modifier) {
            items(
                history.itemCount,
                key = history.itemKey { it.id }
            ) { index ->
                val scrobble = history[index]
                scrobble?.let {
                    HistoryEntry(
                        title = scrobble.track,
                        artist = scrobble.artist,
                        album = scrobble.album,
                        timestamp = scrobble.timestamp,
                        isNowPlaying = false,

                        onClick = {
                            if (scrobble.album != null)
                                bottomSheetState.showAlbumAndTrack(
                                    scrobble.artist, scrobble.track, scrobble.albumArtist, scrobble.album
                                )
                            else
                                bottomSheetState.showTrack(scrobble.artist, scrobble.track)
                        },
                        onMore = {},
                        modifier = Modifier.padding(5.dp).height(84.dp)
                    )
                }
            }
        }
    }

    InfoBottomSheet(
        bottomSheetState,
        navigateToTrackHistory = navigateToTrackHistory,
        navigateToTrack = navigateToTrack,
        navigateToAlbum = navigateToAlbum,
        navigateToArtist = navigateToArtist
    )
}
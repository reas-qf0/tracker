package org.reas.tracker.ui.screens

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import org.reas.tracker.ui.viewmodels.HistoryScreenViewModel
import org.reas.tracker.ui.viewmodels.ViewModelProvider
import org.reas.tracker.ui.components.InfoBottomSheet
import org.reas.tracker.ui.components.showAlbumAndTrack
import org.reas.tracker.ui.components.showTrack

@Composable
fun HistoryScreen(
    navigateToArtist: (String) -> Unit,
    navigateToAlbum: (String, String) -> Unit,
    navigateToTrack: (String, String, String?) -> Unit,
    navigateToTrackHistory: (String, String, String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryScreenViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val nowPlaying by viewModel.nowPlaying.collectAsState()
    val history = viewModel.history.collectAsLazyPagingItems()
    val bottomSheetState = remember { mutableStateOf<BottomSheetInfo?>(null) }

    LazyColumn(modifier = modifier) {
        items(nowPlaying) { scrobble ->
            HistoryEntry(
                title = scrobble.track,
                artist = scrobble.artist,
                album = scrobble.album,
                timestamp = scrobble.timestamp,
                isNowPlaying = true,

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

    InfoBottomSheet(
        bottomSheetState,
        navigateToTrackHistory = navigateToTrackHistory,
        navigateToTrack = navigateToTrack,
        navigateToAlbum = navigateToAlbum,
        navigateToArtist = navigateToArtist
    )
}
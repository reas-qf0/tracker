package com.reas.tracker2.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.reas.tracker2.ui.components.HistoryEntry
import com.reas.tracker2.ui.navigation.BottomSheetInfo
import com.reas.tracker2.ui.navigation.TrackHistory
import com.reas.tracker2.ui.viewmodels.TrackHistoryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TrackHistoryScreen(
    arguments: TrackHistory,
    modifier: Modifier = Modifier,
    navigateToBottomSheet: (BottomSheetInfo) -> Unit,
    viewModel: TrackHistoryViewModel = koinViewModel()
) {
    val artist = arguments.artist
    val track = arguments.track
    val album = arguments.album

    val trackPlays by remember { viewModel.trackPlays(artist, track, album) }.collectAsState()
    val history = remember { viewModel.history(artist, track, album) }.collectAsLazyPagingItems()

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

                        imageUrl = {
                            scrobble.album?.let { viewModel.getAlbumImageUrl(scrobble.artist, it) }
                        },
                        onClick = {
                            navigateToBottomSheet(
                                if (scrobble.album != null)
                                    BottomSheetInfo(
                                        artist = scrobble.artist,
                                        track = scrobble.track,
                                        album = scrobble.album,
                                        albumArtist = scrobble.albumArtist
                                    )
                                else
                                    BottomSheetInfo(
                                        artist = scrobble.artist,
                                        track = scrobble.track
                                    )
                            )
                        },
                        onMore = {},
                        modifier = Modifier.padding(5.dp).height(84.dp)
                    )
                }
            }
        }
    }
}
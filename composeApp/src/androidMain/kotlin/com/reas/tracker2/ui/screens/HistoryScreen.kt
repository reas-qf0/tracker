package com.reas.tracker2.ui.screens

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.reas.tracker2.ui.components.HistoryEntry
import com.reas.tracker2.ui.viewmodels.HistoryScreenViewModel
import com.reas.tracker2.ui.viewmodels.ViewModelProvider
import com.reas.tracker2.ui.navigation.BottomSheetInfo

@Composable
fun HistoryScreen(
    navigateToBottomSheet: (BottomSheetInfo) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryScreenViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val nowPlaying by viewModel.nowPlaying.collectAsState()
    val history = viewModel.history.collectAsLazyPagingItems()

    LazyColumn(modifier = modifier) {
        items(nowPlaying) { scrobble ->
            HistoryEntry(
                title = scrobble.track,
                artist = scrobble.artist,
                album = scrobble.album,
                timestamp = scrobble.timestamp,
                isNowPlaying = true,

                imageUrl = { viewModel.getImageUrl(scrobble) },
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

                    imageUrl = { viewModel.getImageUrl(scrobble) },
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
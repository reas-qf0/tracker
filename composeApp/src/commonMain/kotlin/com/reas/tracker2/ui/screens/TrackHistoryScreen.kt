package com.reas.tracker2.ui.screens

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.reas.tracker2.ui.components.HistoryEndIndicator
import com.reas.tracker2.ui.components.HistoryEntry
import com.reas.tracker2.ui.navigation.ApplicationState
import com.reas.tracker2.ui.navigation.BottomSheetInfo
import com.reas.tracker2.ui.navigation.TrackHistory
import com.reas.tracker2.ui.viewmodels.TrackHistoryViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TrackHistoryScreen(
    arguments: TrackHistory,
    applicationState: ApplicationState,
    modifier: Modifier = Modifier,
    viewModel: TrackHistoryViewModel = koinViewModel()
) {
    val columnState = rememberLazyListState()
    val showButton by remember { derivedStateOf { columnState.firstVisibleItemIndex > 0 } }
    val scope = rememberCoroutineScope()
    applicationState.floatingActionButton(
        visibleIf = showButton,
        onClick = {
            scope.launch {
                columnState.animateScrollToItem(0)
            }
        }
    ) {
        Icon(imageVector = Icons.Filled.ArrowUpward, contentDescription = "Scroll to top")
    }

    val track = arguments.track

    val trackPlays by remember { viewModel.trackPlays(track) }.collectAsStateWithLifecycle()
    val history = remember { viewModel.history(track) }.collectAsLazyPagingItems()

    applicationState.setTitle("${track.artist} - ${track.track}")
    LazyColumn(state = columnState, modifier = modifier) {
        item(key = "header") {
            Text(
                "Plays: $trackPlays",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
            )
        }

        items(
            history.itemCount,
            key = history.itemKey { scrobble -> scrobble.key }
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
                        applicationState.navigate(BottomSheetInfo(track = scrobble.asTrackWithAlbum))
                    },
                    onMore = {},
                    modifier = Modifier.padding(5.dp).height(84.dp)
                )
            }
        }

        if (showButton) {
            item(key = "bottom") {
                HistoryEndIndicator(Modifier.height(75.dp))
            }
        }
    }
}
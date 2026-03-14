package com.reas.tracker2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.reas.tracker2.ui.components.HistoryEntry
import com.reas.tracker2.ui.navigation.ApplicationState
import com.reas.tracker2.ui.navigation.BottomSheetInfo
import com.reas.tracker2.ui.viewmodels.HistoryScreenViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tracker2.composeapp.generated.resources.Res
import tracker2.composeapp.generated.resources.history

@Composable
fun HistoryScreen(
    applicationState: ApplicationState,
    modifier: Modifier = Modifier,
    viewModel: HistoryScreenViewModel = koinViewModel()
) {
    val columnState = rememberLazyListState()
    val showButton by remember { derivedStateOf { columnState.firstVisibleItemIndex > 0 } }
    val scope = rememberCoroutineScope()

    applicationState.setTitle(stringResource(Res.string.history))
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

    val history = viewModel.history.collectAsLazyPagingItems()
    LazyColumn(state = columnState, modifier = modifier) {
        // add an empty item so that the list doesn't jump down when scrolled to the very top
        item(key = "top") {
            Spacer(Modifier.height(2.dp))
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
                    isNowPlaying = scrobble.isNowPlaying,

                    imageUrl = { viewModel.getImageUrl(scrobble) },
                    onClick = {
                        applicationState.navigate(BottomSheetInfo(track = scrobble.asTrackWithAlbum))
                    },
                    onMore = {},
                    modifier = Modifier.padding(5.dp).height(84.dp)
                )
            }
        }

        // empty space at the bottom so that the button doesn't overlap with the entry
        if (showButton) {
            item(key = "bottom") {
                Box(
                    modifier = Modifier.height(75.dp).fillMaxWidth().background(
                        brush = Brush.verticalGradient(colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        ))
                    )
                )
            }
        }
    }
}
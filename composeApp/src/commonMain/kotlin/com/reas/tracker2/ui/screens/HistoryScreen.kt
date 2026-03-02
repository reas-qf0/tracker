package com.reas.tracker2.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.reas.tracker2.ui.components.HistoryEntry
import com.reas.tracker2.ui.viewmodels.HistoryScreenViewModel
import com.reas.tracker2.ui.navigation.BottomSheetInfo
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryScreen(
    navigateToBottomSheet: (BottomSheetInfo) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryScreenViewModel = koinViewModel()
) {
    val history = viewModel.history.collectAsLazyPagingItems()

    LazyColumn(modifier = modifier) {
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
                        navigateToBottomSheet(BottomSheetInfo(track = scrobble.metadata.info))
                    },
                    onMore = {},
                    modifier = Modifier.padding(5.dp).height(84.dp)
                )
            }
        }
    }
}
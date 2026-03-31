package com.reas.tracker2.ui.screens

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.reas.tracker2.ui.components.HistoryEntry
import com.reas.tracker2.ui.components.LazyColumnWithScrollButton
import com.reas.tracker2.ui.navigation.ApplicationState
import com.reas.tracker2.ui.navigation.BottomSheetInfo
import com.reas.tracker2.ui.viewmodels.HistoryScreenViewModel
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
    applicationState.setTitle(stringResource(Res.string.history))

    val history = viewModel.history.collectAsLazyPagingItems()
    LazyColumnWithScrollButton(applicationState, modifier = modifier) {
        items(
            history.itemCount,
            key = history.itemKey { scrobble -> scrobble.key }
        ) { index ->
            val scrobble = history[index]
            scrobble?.let {
                HistoryEntry(
                    title = scrobble.track,
                    artist = scrobble.artistsAsString,
                    album = scrobble.album,
                    timestamp = scrobble.timestamp,
                    isNowPlaying = scrobble.isNowPlaying,
                    associatedEvents = scrobble.associatedEvents,

                    imageUrl = { viewModel.getImageUrl(scrobble) },
                    onClick = {
                        applicationState.navigate(BottomSheetInfo(track = scrobble.metadata))
                    },
                    onMore = {},
                    modifier = Modifier.padding(5.dp).height(84.dp)
                )
            }
        }
    }
}
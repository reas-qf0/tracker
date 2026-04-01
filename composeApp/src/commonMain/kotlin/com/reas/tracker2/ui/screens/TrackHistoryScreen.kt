package com.reas.tracker2.ui.screens

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.itemKey
import com.reas.tracker2.ui.components.HistoryEntry
import com.reas.tracker2.ui.components.LazyColumnWithScrollButton
import com.reas.tracker2.ui.navigation.ApplicationState
import com.reas.tracker2.ui.navigation.BottomSheetInfo
import com.reas.tracker2.ui.navigation.TrackHistory
import com.reas.tracker2.ui.rememberAsPagingItems
import com.reas.tracker2.ui.rememberAsState
import com.reas.tracker2.ui.viewmodels.TrackHistoryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TrackHistoryScreen(
    arguments: TrackHistory,
    applicationState: ApplicationState,
    modifier: Modifier = Modifier,
    viewModel: TrackHistoryViewModel = koinViewModel()
) {
    val track = arguments.track

    val trackPlays by rememberAsState { viewModel.trackPlays(track) }
    val history = rememberAsPagingItems { viewModel.history(track) }

    applicationState.setTitle("${track.artistsAsString} - ${track.name}")
    LazyColumnWithScrollButton(applicationState, modifier = modifier) {
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
                    play = scrobble,
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
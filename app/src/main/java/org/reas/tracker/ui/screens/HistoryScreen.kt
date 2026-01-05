package org.reas.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.reas.tracker.ui.components.BottomSheetInfo
import org.reas.tracker.util.DateTimeFormatter.dateTimeToString
import org.reas.tracker.util.DateTimeFormatter.dateToString
import org.reas.tracker.ui.viewmodels.HistoryScreenViewModel
import org.reas.tracker.ui.viewmodels.ViewModelProvider
import org.reas.tracker.ui.components.ListEntryWithImage
import org.reas.tracker.ui.components.InfoBottomSheet
import org.reas.tracker.ui.components.showAlbumAndTrack
import org.reas.tracker.ui.components.showTrack
import org.reas.tracker.ui.theme.TrackerTheme
import org.reas.tracker.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
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
    val history by viewModel.history.collectAsState()
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

        items(history) { scrobble ->
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

    InfoBottomSheet(
        bottomSheetState,
        navigateToTrackHistory = navigateToTrackHistory,
        navigateToTrack = navigateToTrack,
        navigateToAlbum = navigateToAlbum,
        navigateToArtist = navigateToArtist
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryEntry(
    title: String,
    artist: String,
    album: String?,
    timestamp: Long,
    isNowPlaying: Boolean,
    onClick: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isNowPlaying)
        MaterialTheme.colorScheme.surfaceContainerHighest
    else
        MaterialTheme.colorScheme.surfaceContainer
    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()

    ListEntryWithImage(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(5.dp))
            .clickable(onClick = onClick)
            .padding(5.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    color = MaterialTheme.colorScheme.primary,
                    style = Typography.titleLarge,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1.0F).padding(top = 2.dp)
                )
                Spacer(Modifier.width(5.dp))
                Icon(
                    Icons.Filled.MoreVert,
                    "More",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onMore)
                )
            }
            Spacer(Modifier.height(2.dp))
            Row {
                Column(modifier = Modifier.weight(1.0F)) {
                    Text(
                        artist,
                        style = Typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    album?.let {
                        Spacer(Modifier.height(1.dp))
                        Text(
                            it,
                            style = Typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.width(5.dp))
                Column(
                    modifier = Modifier.fillMaxHeight().padding(bottom = 4.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (isNowPlaying)
                        Icon(
                            Icons.Filled.PlayArrow,
                            "Now Playing",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    else {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = {
                                PlainTooltip(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(dateTimeToString(timestamp))
                                }
                            },
                            state = tooltipState
                        ) {
                            Text(
                                dateToString(timestamp),
                                style = Typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.clickable(onClick = {
                                    scope.launch { tooltipState.show() }
                                })
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 500, heightDp = 84)
@Composable
private fun HistoryEntryPreview() {
    TrackerTheme {
        Scaffold { innerPadding ->
            HistoryEntry(
                title = "Really Long Track Name 00000000000000000000000",
                artist = "Really Long Artist Name 00000000000000000000000",
                album = "Really Long album Name 00000000000000000000000",
                timestamp = System.currentTimeMillis(),
                isNowPlaying = false,

                onClick = {},
                onMore = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Preview(widthDp = 500, heightDp = 84)
@Composable
private fun HistoryEntryPreviewDark() {
    TrackerTheme(darkTheme = true) {
        Scaffold { innerPadding ->
            HistoryEntry(
                title = "Really Long Track Name 00000000000000000000000",
                artist = "Really Long Artist Name 00000000000000000000000",
                album = "Really Long album Name 00000000000000000000000",
                timestamp = System.currentTimeMillis(),
                isNowPlaying = false,

                onClick = {},
                onMore = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Preview(widthDp = 500, heightDp = 84)
@Composable
private fun HistoryEntryNoAlbumPreview() {
    TrackerTheme {
        Scaffold { innerPadding ->
            HistoryEntry(
                title = "Really Long Track Name 00000000000000000000000",
                artist = "Really Long Artist Name 00000000000000000000000",
                album = null,
                timestamp = System.currentTimeMillis(),
                isNowPlaying = false,

                onClick = {},
                onMore = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Preview(widthDp = 500, heightDp = 84)
@Composable
private fun HistoryEntryNoAlbumPreviewDark() {
    TrackerTheme(darkTheme = true) {
        Scaffold { innerPadding ->
            HistoryEntry(
                title = "Really Long Track Name 00000000000000000000000",
                artist = "Really Long Artist Name 00000000000000000000000",
                album = null,
                timestamp = System.currentTimeMillis(),
                isNowPlaying = false,

                onClick = {},
                onMore = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Preview(widthDp = 500, heightDp = 84)
@Composable
private fun HistoryEntryNpPreview() {
    TrackerTheme {
        Scaffold { innerPadding ->
            HistoryEntry(
                title = "Really Long Track Name 00000000000000000000000",
                artist = "Really Long Artist Name 00000000000000000000000",
                album = "Really Long album Name 00000000000000000000000",
                timestamp = System.currentTimeMillis(),
                isNowPlaying = true,

                onClick = {},
                onMore = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Preview(widthDp = 500, heightDp = 84)
@Composable
private fun HistoryEntryNpPreviewDark() {
    TrackerTheme(darkTheme = true) {
        Scaffold { innerPadding ->
            HistoryEntry(
                title = "Really Long Track Name 00000000000000000000000",
                artist = "Really Long Artist Name 00000000000000000000000",
                album = "Really Long album Name 00000000000000000000000",
                timestamp = System.currentTimeMillis(),
                isNowPlaying = true,

                onClick = {},
                onMore = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
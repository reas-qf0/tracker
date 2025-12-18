package org.reas.tracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.State
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.reas.tracker.ui.ViewModelProvider
import org.reas.tracker.ui.theme.TrackerTheme
import org.reas.tracker.ui.theme.Typography
import java.text.SimpleDateFormat
import java.util.Date

private data class BottomSheetInfo(
    val track: String,
    val artist: String,
    val album: String?,
    val albumArtist: String
)

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
    val state by viewModel.history.collectAsState()
    var bottomSheet: BottomSheetInfo? by remember { mutableStateOf(null) }

    LazyColumn(modifier = modifier) {
        items(
            state.history.filter { it.isNowPlaying }.plus(state.history.filter { it.isFull })
        ) { scrobble ->
            HistoryEntry(
                title = scrobble.track,
                artist = scrobble.artist,
                album = scrobble.album,
                timestamp = scrobble.timestamp,
                isNowPlaying = scrobble.isNowPlaying,

                onClick = { bottomSheet = BottomSheetInfo(
                    scrobble.track,
                    scrobble.artist,
                    scrobble.album,
                    scrobble.albumArtist
                ) },
                onMore = {},
                modifier = Modifier.padding(5.dp).height(84.dp)
            )
        }
    }

    if (bottomSheet != null) {
        val track = bottomSheet!!.track
        val artist = bottomSheet!!.artist
        val album = bottomSheet!!.album
        val albumArtist = bottomSheet!!.albumArtist

        val artistPlays by viewModel.artistPlays(artist).collectAsState()
        val artistTimePlayed by viewModel.artistTimePlayed(artist).collectAsState()
        val trackPlays by viewModel.trackPlays(artist, track).collectAsState()
        val trackTimePlayed by viewModel.trackTimePlayed(artist, track).collectAsState()

        var albumPlays: State<String>? = null
        var albumTimePlayed: State<String>? = null
        album?.let {
            albumPlays = viewModel.albumPlays(albumArtist, album).collectAsState()
            albumTimePlayed = viewModel.albumTimePlayed(albumArtist, album).collectAsState()
        }

        ModalBottomSheet(
            onDismissRequest = { bottomSheet = null },
            modifier = modifier
        ) {
            Column {
                HistoryBottomSheetComponent(
                    icon = Icons.Filled.MusicNote,
                    iconDescription = "Track",
                    header = track,
                    buttonContents = listOf(
                        "Track plays" to trackPlays,
                        "Time listened" to trackTimePlayed
                    ),
                    onMainButton = {
                        bottomSheet = null
                        navigateToTrackHistory(artist, track, album)
                    },
                    onMore = {
                        bottomSheet = null
                        navigateToTrack(artist, track, album)
                    }
                )
                BottomSheetSpacer()
                HistoryBottomSheetComponent(
                    icon = Icons.Filled.Person,
                    iconDescription = "Artist",
                    header = artist,
                    buttonContents = listOf(
                        "Artist plays" to artistPlays,
                        "Time listened" to artistTimePlayed
                    ),
                    onMore = {
                        bottomSheet = null
                        navigateToArtist(artist)
                    }
                )
                album?.let {
                    BottomSheetSpacer()
                    HistoryBottomSheetComponent(
                        icon = Icons.Filled.Album,
                        iconDescription = "Album",
                        header = album,
                        buttonContents = listOf(
                            "Album plays" to albumPlays!!.value,
                            "Time listened" to albumTimePlayed!!.value
                        ),
                        onMore = {
                            bottomSheet = null
                            navigateToAlbum(albumArtist, album)
                        }
                    )
                }
            }
        }
    }
}

private val dateFormatter = SimpleDateFormat.getDateInstance()
private val dateTimeFormatter = SimpleDateFormat.getDateTimeInstance()
private fun formatDate(timestamp: Long): String {
    val secondsPassed = (System.currentTimeMillis() - timestamp) / 1000
    if (secondsPassed < 60L)
        return "$secondsPassed secs ago"
    if (secondsPassed < 60L * 60L)
        return "${secondsPassed / 60} mins ago"
    if (secondsPassed < 60L * 60L * 24L)
        return "${secondsPassed / 60 / 60} hrs ago"
    return dateFormatter.format(Date(timestamp))
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

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(5.dp))
            .clickable(onClick = onClick)
            .padding(5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1.0F)
                .clip(shape = RoundedCornerShape(5.dp))
                .background(color = Color.Gray)
        )
        Spacer(Modifier.width(10.dp))
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
                                    Text(dateTimeFormatter.format(Date(timestamp)))
                                }
                            },
                            state = tooltipState
                        ) {
                            Text(
                                formatDate(timestamp),
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

@Composable
private fun BottomSheetSpacer() {
    Spacer(Modifier.height(5.dp))
    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
    Spacer(Modifier.height(15.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryBottomSheetComponent(
    icon: ImageVector,
    iconDescription: String,
    header: String,
    buttonContents: List<Pair<String, String>>,
    onMore: () -> Unit,
    onMainButton: () -> Unit = onMore
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = { expanded = !expanded })
    ) {
        Spacer(Modifier.width(5.dp))
        Icon(icon, iconDescription, tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(5.dp))
        Text(header,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = if (expanded) Int.MAX_VALUE else 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    Spacer(Modifier.height(5.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        Button(
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                MaterialTheme.colorScheme.secondary
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
            contentPadding = PaddingValues(0.dp),
            onClick = onMainButton,
            modifier = Modifier.weight(2.0F).padding(5.dp).height(100.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                buttonContents.forEach { (line1, line2) ->
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(line1, style = MaterialTheme.typography.bodySmall)
                        Text(line2, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        Button(
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(
                MaterialTheme.colorScheme.surfaceContainerHighest,
                MaterialTheme.colorScheme.primary
            ),
            contentPadding = PaddingValues(0.dp),
            onClick = onMore,
            modifier = Modifier.weight(1.0F).padding(5.dp).height(100.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "See more")
                Text("More info", style = MaterialTheme.typography.bodyMedium)
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
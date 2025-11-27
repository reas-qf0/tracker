package org.reas.tracker.ui.screens

import android.R.attr.maxLines
import android.R.attr.track
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.reas.tracker.ui.theme.TrackerTheme
import org.reas.tracker.ui.theme.Typography
import java.text.SimpleDateFormat
import java.util.Date

data class Scrobble(
    val track: String,
    val artist: String,
    val album: String?,
    val timestamp: Long
)

val scrobbles = List(100) { i ->
    Scrobble(
        "Placeholder Track $i",
        "Placeholder Artist $i",
        if (i % 2 == 1) null else "Placeholder Album $i",
        System.currentTimeMillis() / 1000 - 60 * i * i
    )
}

@Composable
fun HistoryScreen(
    navigateToArtist: (String) -> Unit,
    navigateToAlbum: (String, String) -> Unit,
    navigateToTrack: (String, String, String?) -> Unit,
    navigateToTrackHistory: (String, String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn {
        items(scrobbles) { scrobble ->
            var showBottomSheet by remember { mutableStateOf(false) }
            HistoryEntry(
                scrobble.track,
                scrobble.artist,
                scrobble.album,
                scrobble.timestamp,

                onClick = { showBottomSheet = true },
                modifier = Modifier.padding(5.dp)
            )
            if (showBottomSheet) {
                HistoryBottomSheet(
                    scrobble.track,
                    scrobble.artist,
                    scrobble.album,
                    onDismiss = { showBottomSheet = false },
                    onClickArtist = {
                        showBottomSheet = false
                        navigateToArtist(scrobble.artist)
                    },
                    onClickAlbum = {
                        showBottomSheet = false
                        navigateToAlbum(scrobble.artist, scrobble.album!!)
                    },
                    onClickTrack = {
                        showBottomSheet = false
                        navigateToTrack(scrobble.artist, scrobble.track, scrobble.album)
                    },
                    onClickTrackHistory = {
                        showBottomSheet = false
                        navigateToTrackHistory(scrobble.artist, scrobble.track, scrobble.album)
                    }
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val secondsPassed = System.currentTimeMillis() / 1000 - timestamp
    if (secondsPassed < 60L)
        return "$secondsPassed secs ago"
    if (secondsPassed < 60L * 60L)
        return "${secondsPassed / 60} mins ago"
    if (secondsPassed < 60L * 60L * 24L)
        return "${secondsPassed / 60 / 60} hrs ago"
    return SimpleDateFormat.getDateInstance().format(Date(timestamp * 1000))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryEntry(
    title: String,
    artist: String,
    album: String?,
    timestamp: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(5.dp),
        colors = ButtonDefaults.buttonColors(
            MaterialTheme.colorScheme.surfaceContainerHigh,
            MaterialTheme.colorScheme.primary
        ),
        contentPadding = PaddingValues(5.dp),
        modifier = modifier.height(84.dp)
    ) {
        Row {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1.0F)
                    .clip(shape = RoundedCornerShape(5.dp))
                    .background(color = Color.Gray)
            )
            Spacer(Modifier.width(10.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxHeight().weight(1.0F)
            ) {
                Text(
                    title,
                    style = Typography.titleLarge,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    artist,
                    style = Typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                album?.let {
                    Text(
                        it,
                        style = Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxHeight()
                    .padding(top = 5.dp, bottom = 4.dp, end = 5.dp)
            ) {
                Icon(Icons.Filled.MoreVert, "More")
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                SimpleDateFormat.getDateTimeInstance()
                                    .format(Date(timestamp * 1000))
                            )
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    Text(
                        formatDate(timestamp),
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
    track: String,
    artist: String,
    album: String?,
    onClickArtist: () -> Unit,
    onClickAlbum: () -> Unit,
    onClickTrack: () -> Unit,
    onClickTrackHistory: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column {
            HistoryBottomSheetComponent(
                modifier = Modifier.padding(10.dp),
                icon = { Icon(Icons.Filled.MusicNote, "Track", tint = MaterialTheme.colorScheme.secondary) },
                header = track,
                buttonContents = {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Track plays", style = MaterialTheme.typography.bodySmall)
                        Text("6", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Time listened", style = MaterialTheme.typography.bodySmall)
                        Text("100d 00:12", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    }
                },
                onMainButton = onClickTrackHistory,
                onMore = onClickTrack
            )
            Spacer(Modifier.height(5.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            Spacer(Modifier.height(15.dp))
            HistoryBottomSheetComponent(
                modifier = Modifier.padding(10.dp),
                icon = { Icon(Icons.Filled.Person, "Artist", tint = MaterialTheme.colorScheme.secondary) },
                header = artist,
                buttonContents = {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Artist plays", style = MaterialTheme.typography.bodySmall)
                        Text("600", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Time listened", style = MaterialTheme.typography.bodySmall)
                        Text("100d 00:12", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    }
                },
                onMainButton = onClickArtist,
                onMore = onClickArtist
            )
            album?.let {
                Spacer(Modifier.height(5.dp))
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                Spacer(Modifier.height(15.dp))
                HistoryBottomSheetComponent(
                    modifier = Modifier.padding(10.dp),
                    icon = { Icon(Icons.Filled.Album, "Album", tint = MaterialTheme.colorScheme.secondary) },
                    header = album,
                    buttonContents = {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Artist plays", style = MaterialTheme.typography.bodySmall)
                            Text("60", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Time listened", style = MaterialTheme.typography.bodySmall)
                            Text("100d 00:12", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    onMainButton = onClickAlbum,
                    onMore = onClickAlbum
                )
            }
        }
    }
}

@Composable
private fun HistoryBottomSheetComponent(
    icon: @Composable () -> Unit,
    header: String,
    buttonContents: @Composable () -> Unit,
    onMainButton: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.width(5.dp))
        icon()
        Spacer(Modifier.width(5.dp))
        Text(header,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1, overflow = TextOverflow.Ellipsis
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
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                buttonContents()
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
                "Placeholder Track",
                "Placeholder Artist",
                "Placeholder Album",
                System.currentTimeMillis() / 1000,

                onClick = {},
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
                "Placeholder Track",
                "Placeholder Artist",
                "Placeholder Album",
                System.currentTimeMillis() / 1000,

                onClick = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Preview
@Composable
private fun HistoryBottomSheetPreview() {
    TrackerTheme {
        Scaffold { innerPadding ->
            HistoryBottomSheet(
                "Placeholder Track",
                "Placeholder Artist",
                "Placeholder Album",
                onDismiss = {},
                onClickArtist = {},
                onClickAlbum = {},
                onClickTrack = {},
                onClickTrackHistory = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Preview
@Composable
private fun HistoryBottomSheetPreviewDark() {
    TrackerTheme(darkTheme = true) {
        Scaffold { innerPadding ->
            HistoryBottomSheet(
                "Placeholder Track",
                "Placeholder Artist",
                "Placeholder Album",
                onDismiss = {},
                onClickArtist = {},
                onClickAlbum = {},
                onClickTrack = {},
                onClickTrackHistory = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
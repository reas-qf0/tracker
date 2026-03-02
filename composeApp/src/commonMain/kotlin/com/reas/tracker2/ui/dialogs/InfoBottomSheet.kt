package com.reas.tracker2.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reas.tracker2.ui.navigation.*
import com.reas.tracker2.ui.viewmodels.InfoBottomSheetsViewModel
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tracker2.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoBottomSheet(
    arguments: BottomSheetInfo,
    onDismiss: () -> Unit,
    navigateToArtist: (ArtistInfo) -> Unit,
    navigateToAlbum: (AlbumInfo) -> Unit,
    navigateToTrack: (TrackInfo) -> Unit,
    navigateToTrackHistory: (TrackHistory) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InfoBottomSheetsViewModel = koinViewModel()
) {
    val track = arguments.track?.track
    val album = arguments.track?.album ?: arguments.album?.title
    val artist = arguments.track?.artist ?: arguments.album?.artist ?: arguments.artist!!
    val albumArtist = arguments.track?.albumArtist ?: arguments.album?.artist

    val trackO = arguments.track
    val albumO = arguments.track?._album ?: arguments.album

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        modifier = modifier
    ) {
        Column {
            track?.let {
                val trackPlays by remember { viewModel.trackPlays(trackO) }.collectAsStateWithLifecycle()
                val trackTimePlayed by remember { viewModel.trackTimePlayed(trackO) }.collectAsStateWithLifecycle()
                HistoryBottomSheetComponent(
                    icon = Icons.Filled.MusicNote,
                    iconDescription = stringResource(Res.string.track),
                    header = track,
                    buttonContents = listOf(
                        Res.string.track_plays to trackPlays,
                        Res.string.time_listened to trackTimePlayed
                    ),
                    onMainButton = {
                        navigateToTrackHistory(TrackHistory(trackO))
                    },
                    onMore = {
                        navigateToTrack(TrackInfo(trackO))
                    }
                )
                BottomSheetSpacer()
            }

            val artistPlays by remember { viewModel.artistPlays(artist) }.collectAsStateWithLifecycle()
            val artistTimePlayed by remember { viewModel.artistTimePlayed(artist) }.collectAsStateWithLifecycle()
            HistoryBottomSheetComponent(
                icon = Icons.Filled.Person,
                iconDescription = stringResource(Res.string.artist),
                header = artist,
                buttonContents = listOf(
                    Res.string.artist_plays to artistPlays,
                    Res.string.time_listened to artistTimePlayed
                ),
                onMore = {
                    navigateToArtist(ArtistInfo(artist))
                }
            )

            album?.let {
                albumO!!
                val albumPlays by remember { viewModel.albumPlays(albumO) }.collectAsStateWithLifecycle()
                val albumTimePlayed by remember { viewModel.albumTimePlayed(albumO) }.collectAsStateWithLifecycle()
                BottomSheetSpacer()
                HistoryBottomSheetComponent(
                    icon = Icons.Filled.Album,
                    iconDescription = stringResource(Res.string.album),
                    header = album,
                    buttonContents = listOf(
                        Res.string.album_plays to albumPlays,
                        Res.string.time_listened to albumTimePlayed
                    ),
                    onMore = {
                        navigateToAlbum(AlbumInfo(albumO))
                    }
                )
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
    buttonContents: List<Pair<StringResource, String>>,
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
            modifier = Modifier
                .weight(2.0F)
                .padding(5.dp)
                .height(100.dp)
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
                        Text(
                            stringResource(line1),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            line2,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
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
            modifier = Modifier
                .weight(1.0F)
                .padding(5.dp)
                .height(100.dp)
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
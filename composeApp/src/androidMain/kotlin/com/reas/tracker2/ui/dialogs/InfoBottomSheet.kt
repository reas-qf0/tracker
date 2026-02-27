package com.reas.tracker2.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reas.tracker2.R
import com.reas.tracker2.ui.navigation.AlbumInfo
import com.reas.tracker2.ui.navigation.ArtistInfo
import com.reas.tracker2.ui.navigation.BottomSheetInfo
import com.reas.tracker2.ui.navigation.TrackHistory
import com.reas.tracker2.ui.navigation.TrackInfo
import com.reas.tracker2.ui.viewmodels.InfoBottomSheetsViewModel
import org.koin.compose.viewmodel.koinViewModel

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
    val track = arguments.track
    val album = arguments.album
    val artist = arguments.artist
    val albumArtist = arguments.albumArtist

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        modifier = modifier
    ) {
        Column {
            track?.let {
                val trackPlays by remember { viewModel.trackPlays(artist, track, album) }.collectAsStateWithLifecycle()
                val trackTimePlayed by remember { viewModel.trackTimePlayed(artist, track, album) }.collectAsStateWithLifecycle()
                HistoryBottomSheetComponent(
                    icon = Icons.Filled.MusicNote,
                    iconDescription = stringResource(R.string.track),
                    header = track,
                    buttonContents = listOf(
                        R.string.track_plays to trackPlays,
                        R.string.time_listened to trackTimePlayed
                    ),
                    onMainButton = {
                        navigateToTrackHistory(TrackHistory(artist, track, album))
                    },
                    onMore = {
                        navigateToTrack(TrackInfo(artist, album, track))
                    }
                )
                BottomSheetSpacer()
            }

            val artistPlays by remember { viewModel.artistPlays(artist) }.collectAsStateWithLifecycle()
            val artistTimePlayed by remember { viewModel.artistTimePlayed(artist) }.collectAsStateWithLifecycle()
            HistoryBottomSheetComponent(
                icon = Icons.Filled.Person,
                iconDescription = stringResource(R.string.artist),
                header = artist,
                buttonContents = listOf(
                    R.string.artist_plays to artistPlays,
                    R.string.time_listened to artistTimePlayed
                ),
                onMore = {
                    navigateToArtist(ArtistInfo(artist))
                }
            )

            album?.let {
                albumArtist!!
                val albumPlays by remember { viewModel.albumPlays(albumArtist, album) }.collectAsStateWithLifecycle()
                val albumTimePlayed by remember { viewModel.albumTimePlayed(albumArtist, album) }.collectAsStateWithLifecycle()
                BottomSheetSpacer()
                HistoryBottomSheetComponent(
                    icon = Icons.Filled.Album,
                    iconDescription = stringResource(R.string.album),
                    header = album,
                    buttonContents = listOf(
                        R.string.album_plays to albumPlays,
                        R.string.time_listened to albumTimePlayed
                    ),
                    onMore = {
                        navigateToAlbum(AlbumInfo(albumArtist, album))
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
    buttonContents: List<Pair<Int, String>>,
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
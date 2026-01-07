package org.reas.tracker.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import org.reas.tracker.ui.components.ChartColumn
import org.reas.tracker.ui.components.ListEntryWithImage
import org.reas.tracker.ui.components.SortOrderSelectionChip
import org.reas.tracker.ui.navigation.ArtistInfo
import org.reas.tracker.ui.navigation.BottomSheetInfo
import org.reas.tracker.ui.navigation.ChartSort
import org.reas.tracker.ui.theme.TrackerTheme
import org.reas.tracker.ui.viewmodels.ArtistInfoScreenViewModel
import org.reas.tracker.ui.viewmodels.ViewModelProvider
import org.reas.tracker.util.DateTimeFormatter.timeMsToString

@Composable
fun ArtistInfoScreen(
    arguments: ArtistInfo,
    navigateToArtist: (ArtistInfo) -> Unit,
    navigateToBottomSheet: (BottomSheetInfo) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArtistInfoScreenViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val artist = arguments.artist
    val sort = arguments.sort
    val start = 0L
    val end = Long.MAX_VALUE

    val plays by remember { viewModel.artistPlays(artist, start, end) }.collectAsState()
    val timePlayed by remember { viewModel.artistTimePlayed(artist, start, end) }.collectAsState()
    val rank by remember(plays, timePlayed) {
        when (sort) {
            ChartSort.PLAYS -> viewModel.playRank(plays, start, end)
            ChartSort.TIME -> viewModel.rank(timePlayed, start, end)
        }
    }.collectAsState()
    val albums = remember {
        when (sort) {
            ChartSort.TIME -> viewModel.topAlbums(artist, start, end)
            ChartSort.PLAYS -> viewModel.topAlbumsByPlayCount(artist, start, end)
        }
    }.collectAsLazyPagingItems()
    val tracks = remember {
        when (sort) {
            ChartSort.TIME -> viewModel.topTracks(artist, start, end)
            ChartSort.PLAYS -> viewModel.topTracksByPlayCount(artist, start, end)
        }
    }.collectAsLazyPagingItems()

    Column(
        modifier = modifier.padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        SortOrderSelectionChip(sort, { navigateToArtist(arguments.copy(sort = it)) })
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.verticalScroll(rememberScrollState()),
        ) {
            ListEntryWithImage(
                modifier = Modifier.height(125.dp),
                alignment = Alignment.CenterVertically
            ) {
                Text(
                    artist,
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1.0F)
                )
            }
            Spacer(Modifier.height(5.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.0F)
                        .border(1.dp, Color.Gray, MaterialTheme.shapes.medium)
                        .aspectRatio(1.0F),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(plays.toString(), style = MaterialTheme.typography.headlineSmall)
                        Text("plays", color = MaterialTheme.colorScheme.secondary)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1.0F)
                        .border(1.dp, Color.Gray, MaterialTheme.shapes.medium)
                        .aspectRatio(1.0F),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            timeMsToString(timePlayed),
                            style = MaterialTheme.typography.headlineSmall,
                            autoSize = TextAutoSize.StepBased(
                                maxFontSize = MaterialTheme.typography.headlineSmall.fontSize
                            ),
                            maxLines = 1
                        )
                        Text("time played", color = MaterialTheme.colorScheme.secondary)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1.0F)
                        .border(1.dp, Color.Gray, MaterialTheme.shapes.medium)
                        .aspectRatio(1.0F),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            rank,
                            style = MaterialTheme.typography.headlineSmall,
                            autoSize = TextAutoSize.StepBased(
                                maxFontSize = MaterialTheme.typography.headlineSmall.fontSize
                            ),
                            maxLines = 1
                        )
                        Text(
                            when (sort) {
                                ChartSort.TIME -> "in charts\nby time"
                                ChartSort.PLAYS -> "in charts\nby plays"
                            },
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(5.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 5.dp)
            ) {
                Text("Top albums", style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 10.dp))
                Spacer(Modifier.weight(1.0F))
                AssistChip(
                    onClick = {},
                    label = { Text("More") },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowRight,
                            "More"
                        )
                    }
                )
            }
            ChartColumn(
                albums, limit = 5,
                onClick = { entry -> navigateToBottomSheet(entry.bottomSheetInfo) },
            )

            Spacer(Modifier.height(5.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 5.dp)
            ) {
                Text("Top tracks", style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 10.dp))
                Spacer(Modifier.weight(1.0F))
                AssistChip(
                    onClick = {},
                    label = { Text("More") },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowRight,
                            "More"
                        )
                    }
                )
            }
            ChartColumn(
                tracks, limit = 5,
                onClick = { entry -> navigateToBottomSheet(entry.bottomSheetInfo) },
            )

            Spacer(Modifier.height(5.dp))
        }
    }
}


@Preview
@Composable
fun ArtistInfoScreenPreview() {
    TrackerTheme {
        Scaffold { innerPadding ->
            ArtistInfoScreen(
                ArtistInfo("Artist"),
                navigateToArtist = {},
                navigateToBottomSheet = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}


@Preview
@Composable
fun ArtistInfoScreenPreviewDark() {
    TrackerTheme(darkTheme = true) {
        Scaffold { innerPadding ->
            ArtistInfoScreen(
                ArtistInfo("Artist"),
                navigateToArtist = {},
                navigateToBottomSheet = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
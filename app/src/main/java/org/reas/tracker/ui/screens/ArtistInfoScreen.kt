package org.reas.tracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.MutableStateFlow
import org.reas.tracker.R
import org.reas.tracker.ui.components.AutosizingText
import org.reas.tracker.ui.components.ChartColumn
import org.reas.tracker.ui.components.InfoBox
import org.reas.tracker.ui.components.InfoChartHeader
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

    val plays by remember { viewModel.plays(artist, start, end) }.collectAsState()
    val timePlayed by remember { viewModel.timePlayed(artist, start, end) }.collectAsState()
    val playsAsString = if (plays == -1) "..." else plays.toString()
    val timePlayedAsString = if (timePlayed == -1L) "..." else timeMsToString(timePlayed)
    val rank by remember(plays, timePlayed) {
        when (sort) {
            ChartSort.PLAYS ->
                if (plays == -1) MutableStateFlow("...") else viewModel.playRank(plays, start, end)
            ChartSort.TIME ->
                if (timePlayed == -1L) MutableStateFlow("...") else viewModel.rank(timePlayed, start, end)
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
                AutosizingText(
                    artist,
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.weight(1.0F)
                )
            }
            Spacer(Modifier.height(5.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoBox {
                    AutosizingText(playsAsString, style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(R.string.plays).lowercase(), color = MaterialTheme.colorScheme.secondary)
                }
                InfoBox {
                    AutosizingText(timePlayedAsString, style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(R.string.time_played).lowercase(), color = MaterialTheme.colorScheme.secondary)
                }
                InfoBox {
                    AutosizingText(rank, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        when (sort) {
                            ChartSort.TIME -> stringResource(R.string.in_charts_by_time)
                            ChartSort.PLAYS -> stringResource(R.string.in_charts_by_plays)
                        },
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(5.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            InfoChartHeader(
                stringResource(R.string.top_albums),
                onClick = {},
                modifier = Modifier.padding(top = 5.dp)
            )
            ChartColumn(
                albums, limit = 5,
                onClick = { entry -> navigateToBottomSheet(entry.bottomSheetInfo) },
            )

            Spacer(Modifier.height(5.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            InfoChartHeader(
                stringResource(R.string.top_tracks),
                onClick = {},
                modifier = Modifier.padding(top = 5.dp)
            )
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
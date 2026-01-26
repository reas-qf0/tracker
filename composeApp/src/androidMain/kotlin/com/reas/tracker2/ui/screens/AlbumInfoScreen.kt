package com.reas.tracker2.ui.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.reas.tracker2.R
import kotlinx.coroutines.flow.MutableStateFlow
import com.reas.tracker2.ui.components.AutosizingText
import com.reas.tracker2.ui.components.ChartColumn
import com.reas.tracker2.ui.components.InfoBox
import com.reas.tracker2.ui.components.InfoChartHeader
import com.reas.tracker2.ui.components.ListEntryWithImage
import com.reas.tracker2.ui.components.SortOrderSelectionChip
import com.reas.tracker2.ui.navigation.AlbumInfo
import com.reas.tracker2.ui.navigation.BottomSheetInfo
import com.reas.tracker2.ui.navigation.ChartSort
import com.reas.tracker2.ui.viewmodels.AlbumInfoScreenViewModel
import com.reas.tracker2.util.DateTimeFormatter.timeMsToString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AlbumInfoScreen(
    arguments: AlbumInfo,
    navigateToAlbum: (AlbumInfo) -> Unit,
    navigateToBottomSheet: (BottomSheetInfo) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumInfoScreenViewModel = koinViewModel()
) {
    val artist = arguments.artist
    val album = arguments.album
    val sort = arguments.sort
    val start = 0L
    val end = Long.MAX_VALUE

    val plays by remember { viewModel.plays(artist, album, start, end) }.collectAsState()
    val timePlayed by remember { viewModel.timePlayed(artist, album, start, end) }.collectAsState()
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
    val tracks = remember {
        when (sort) {
            ChartSort.TIME -> viewModel.topTracks(artist, album, start, end)
            ChartSort.PLAYS -> viewModel.topTracksByPlayCount(artist, album, start, end)
        }
    }.collectAsLazyPagingItems()

    Column(
        modifier = modifier.padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        SortOrderSelectionChip(sort, { navigateToAlbum(arguments.copy(sort = it)) })
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.verticalScroll(rememberScrollState()),
        ) {
            ListEntryWithImage(
                modifier = Modifier.height(125.dp),
                alignment = Alignment.CenterVertically,
                url = { viewModel.getAlbumImageUrl(artist, album) }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1.0F)
                ) {
                    AutosizingText(album, style = MaterialTheme.typography.displaySmall)
                    AutosizingText(artist, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
                }
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
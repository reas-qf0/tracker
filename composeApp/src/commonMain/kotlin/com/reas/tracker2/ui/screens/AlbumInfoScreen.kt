package com.reas.tracker2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.reas.tracker2.shared.TimePeriod
import com.reas.tracker2.ui.components.*
import com.reas.tracker2.ui.navigation.AlbumInfo
import com.reas.tracker2.ui.navigation.ApplicationState
import com.reas.tracker2.ui.viewmodels.AlbumInfoScreenViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tracker2.composeapp.generated.resources.*
import kotlin.time.Duration.Companion.minutes

@Composable
fun AlbumInfoScreen(
    arguments: AlbumInfo,
    applicationState: ApplicationState,
    modifier: Modifier = Modifier,
    viewModel: AlbumInfoScreenViewModel = koinViewModel()
) {
    val album = arguments.album
    val sort by viewModel.sort().collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val period = TimePeriod.ALLTIME

    val plays by remember { viewModel.plays(album, period) }.collectAsStateWithLifecycle()
    val timePlayed by remember { viewModel.timePlayed(album, period) }.collectAsStateWithLifecycle()
    val playsAsString = if (plays == -1) "..." else plays.toString()
    val timePlayedAsString = if (timePlayed.isNegative()) "..." else timePlayed.inWholeMinutes.minutes.toString()
    val timeRank by remember { viewModel.rank(album, period) }.collectAsStateWithLifecycle()
    val playRank by remember { viewModel.playRank(album, period) }.collectAsStateWithLifecycle()

    val timeTracks = remember { viewModel.topTracks(album, period) }.collectAsLazyPagingItems()
    val playTracks = remember { viewModel.topTracksByPlayCount(album, period) }.collectAsLazyPagingItems()

    applicationState.setTitle("${album.artistsAsString} - ${album.name}")
    Column(
        modifier = modifier.padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        SortOrderSelectionChip(sort, { scope.launch { viewModel.setSort(it) } })
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.verticalScroll(rememberScrollState()),
        ) {
            ListEntryWithImage(
                modifier = Modifier.height(125.dp),
                alignment = Alignment.CenterVertically,
                url = { viewModel.getAlbumImageUrl(album) }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1.0F)
                ) {
                    AutosizingText(album.name, style = MaterialTheme.typography.displaySmall)
                    album.artists.forEach { artist ->
                        AutosizingText(
                            artist.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            Spacer(Modifier.height(5.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoBox {
                    AutosizingText(playsAsString, style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(Res.string.plays).lowercase(), color = MaterialTheme.colorScheme.secondary)
                }
                InfoBox {
                    AutosizingText(timePlayedAsString, style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(Res.string.time_played).lowercase(), color = MaterialTheme.colorScheme.secondary)
                }
                InfoBox {
                    if (sort.byTime) {
                        AutosizingText(timeRank, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            stringResource(Res.string.in_charts_by_time),
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        AutosizingText(playRank, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            stringResource(Res.string.in_charts_by_plays),
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(5.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            InfoChartHeader(
                stringResource(Res.string.top_tracks),
                onClick = {},
                modifier = Modifier.padding(top = 5.dp)
            )
            DoubleChartColumn(
                sort.byTime, timeTracks, playTracks,
                limit = 5,
                onClick = { entry -> applicationState.navigate(entry.bottomSheetInfo) },
            )

            Spacer(Modifier.height(5.dp))
        }
    }
}
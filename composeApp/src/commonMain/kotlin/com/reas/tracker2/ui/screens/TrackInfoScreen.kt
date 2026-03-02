package com.reas.tracker2.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reas.tracker2.shared.TimePeriod
import kotlinx.coroutines.flow.MutableStateFlow
import com.reas.tracker2.ui.components.AutosizingText
import com.reas.tracker2.ui.components.InfoBox
import com.reas.tracker2.ui.components.ListEntryWithImage
import com.reas.tracker2.ui.components.SortOrderSelectionChip
import com.reas.tracker2.ui.navigation.BottomSheetInfo
import com.reas.tracker2.ui.navigation.ChartSort
import com.reas.tracker2.ui.navigation.TrackInfo
import com.reas.tracker2.ui.viewmodels.TrackInfoScreenViewModel
import com.reas.tracker2.util.DateTimeFormatter.timeMsToString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tracker2.composeapp.generated.resources.Res
import tracker2.composeapp.generated.resources.in_charts_by_plays
import tracker2.composeapp.generated.resources.in_charts_by_time
import tracker2.composeapp.generated.resources.plays
import tracker2.composeapp.generated.resources.time_played
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun TrackInfoScreen(
    arguments: TrackInfo,
    navigateToTrack: (TrackInfo) -> Unit,
    navigateToBottomSheet: (BottomSheetInfo) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrackInfoScreenViewModel = koinViewModel()
) {
    val track = arguments.track
    val sort = arguments.sort
    val period = TimePeriod.ALLTIME

    val plays by remember { viewModel.plays(track, period) }.collectAsStateWithLifecycle()
    val timePlayed by remember { viewModel.timePlayed(track, period) }.collectAsStateWithLifecycle()
    val playsAsString = if (plays == -1) "..." else plays.toString()
    val timePlayedAsString = if (timePlayed.isNegative()) "..." else timePlayed.inWholeMinutes.minutes.toString()
    val rank by remember(plays, timePlayed) {
        when (sort) {
            ChartSort.PLAYS ->
                if (plays == -1) MutableStateFlow("...") else viewModel.playRank(plays, period)
            ChartSort.TIME ->
                if (timePlayed.isNegative()) MutableStateFlow("...") else viewModel.rank(timePlayed, period)
        }
    }.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        SortOrderSelectionChip(sort, { navigateToTrack(arguments.copy(sort = it)) })
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.verticalScroll(rememberScrollState()),
        ) {
            ListEntryWithImage(
                modifier = Modifier.height(125.dp),
                alignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1.0F)
                ) {
                    AutosizingText(track.track, style = MaterialTheme.typography.displaySmall)
                    AutosizingText(
                        track.artist,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    track.album?.let {
                        AutosizingText(
                            track.album!!,
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
                    AutosizingText(
                        timePlayedAsString,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(stringResource(Res.string.time_played).lowercase(), color = MaterialTheme.colorScheme.secondary)
                }
                InfoBox {
                    AutosizingText(rank, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        when (sort) {
                            ChartSort.TIME -> stringResource(Res.string.in_charts_by_time)
                            ChartSort.PLAYS -> stringResource(Res.string.in_charts_by_plays)
                        },
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
package org.reas.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import org.reas.tracker.ui.components.BottomSheetInfo
import org.reas.tracker.ui.components.InfoBottomSheet
import org.reas.tracker.util.DateTimeFormatter.timeMsToString
import org.reas.tracker.ui.components.ListEntryWithImage
import org.reas.tracker.ui.components.showAlbum
import org.reas.tracker.ui.components.showArtist
import org.reas.tracker.ui.components.showTrack
import org.reas.tracker.ui.theme.TrackerTheme
import org.reas.tracker.ui.viewmodels.ChartsScreenViewModel
import org.reas.tracker.ui.viewmodels.ViewModelProvider
import kotlin.enums.enumEntries

private enum class ChartType {
    ARTISTS {
        override val icon: ImageVector
            get() = Icons.Filled.Person
        override val label: String
            get() = "Artists"
    },
    ALBUMS {
        override val icon: ImageVector
            get() = Icons.Filled.Album
        override val label: String
            get() = "Albums"
    },
    TRACKS {
        override val icon: ImageVector
            get() = Icons.Filled.MusicNote
        override val label: String
            get() = "Tracks"
    };

    abstract val icon: ImageVector
    abstract val label: String
}

private enum class ChartSort {
    TIME {
        override val icon: ImageVector
            get() = Icons.Filled.AccessTime
        override val label: String
            get() = "Time"
    }, PLAYS {
        override val icon: ImageVector
            get() = Icons.Filled.PlayArrow
        override val label: String
            get() = "Plays"
    };

    abstract val icon: ImageVector
    abstract val label: String
}

@Composable
fun ChartsScreen(
    navigateToArtist: (String) -> Unit,
    navigateToAlbum: (String, String) -> Unit,
    navigateToTrack: (String, String, String?) -> Unit,
    navigateToTrackHistory: (String, String, String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChartsScreenViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    var chartType by remember { mutableStateOf(ChartType.ARTISTS) }
    var chartSort by remember { mutableStateOf(ChartSort.TIME) }
    var startTimestamp by remember { mutableLongStateOf(0L) }
    var endTimestamp by remember { mutableLongStateOf(Long.MAX_VALUE) }
    val bottomSheetState = remember { mutableStateOf<BottomSheetInfo?>(null) }

    Column(modifier = modifier.padding(horizontal = 5.dp)) {
        Row {
            Box {
                var expanded by remember { mutableStateOf(false) }
                AssistChip(
                    onClick = { expanded = !expanded },
                    label = { Text(chartType.label) },
                    leadingIcon = {
                        Icon(
                            chartType.icon,
                            contentDescription = chartType.label,
                            Modifier.size(AssistChipDefaults.IconSize)
                        )
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    enumEntries<ChartType>().forEach { entry ->
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    entry.icon,
                                    contentDescription = entry.label,
                                    Modifier.size(AssistChipDefaults.IconSize)
                                )
                            },
                            text = { Text(entry.label) },
                            onClick = {
                                chartType = entry
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.width(10.dp))
            Box {
                var expanded by remember { mutableStateOf(false) }
                AssistChip(
                    onClick = { expanded = !expanded },
                    label = { Text(chartSort.label) },
                    leadingIcon = {
                        Icon(
                            chartSort.icon,
                            contentDescription = chartSort.label,
                            Modifier.size(AssistChipDefaults.IconSize)
                        )
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    enumEntries<ChartSort>().forEach { entry ->
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    entry.icon,
                                    contentDescription = entry.label,
                                    Modifier.size(AssistChipDefaults.IconSize)
                                )
                            },
                            text = { Text(entry.label) },
                            onClick = {
                                chartSort = entry
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        if (chartType == ChartType.ARTISTS && chartSort == ChartSort.TIME) {
            val artistsByTime = remember { viewModel.artists(startTimestamp, endTimestamp) }.collectAsLazyPagingItems()
            ChartColumn {
                items(artistsByTime.itemCount) { i ->
                    val entry = artistsByTime[i]
                    entry?.let {
                        ChartEntry(
                            number = i + 1,
                            label = entry.artist,
                            metricAsString = timeMsToString(entry.metric),
                            metric = entry.metric.toDouble() / artistsByTime[0]!!.metric,
                            onClick = { bottomSheetState.showArtist(entry.artist) }
                        )
                    }
                }
            }
        }

        if (chartType == ChartType.ALBUMS && chartSort == ChartSort.TIME) {
            val albumsByTime = remember { viewModel.albums(startTimestamp, endTimestamp) }.collectAsLazyPagingItems()
            ChartColumn {
                items(albumsByTime.itemCount) { i ->
                    val entry = albumsByTime[i]
                    entry?.let {
                        ChartEntry(
                            number = i + 1,
                            label = entry.album,
                            label2 = entry.artist,
                            metricAsString = timeMsToString(entry.metric),
                            metric = entry.metric.toDouble() / albumsByTime[0]!!.metric,
                            onClick = { bottomSheetState.showAlbum(entry.artist, entry.album) }
                        )
                    }
                }
            }
        }

        if (chartType == ChartType.TRACKS && chartSort == ChartSort.TIME) {
            val tracksByTime = remember { viewModel.tracks(startTimestamp, endTimestamp) }.collectAsLazyPagingItems()
            ChartColumn {
                items(tracksByTime.itemCount) { i ->
                    val entry = tracksByTime[i]
                    entry?.let {
                        ChartEntry(
                            number = i + 1,
                            label = entry.track,
                            label2 = entry.artist,
                            metricAsString = timeMsToString(entry.metric),
                            metric = entry.metric.toDouble() / tracksByTime[0]!!.metric,
                            onClick = { bottomSheetState.showTrack(entry.artist, entry.track) }
                        )
                    }
                }
            }
        }

        if (chartType == ChartType.ARTISTS && chartSort == ChartSort.PLAYS) {
            val artistsByPlayCount = remember { viewModel.artistsByPlayCount(startTimestamp, endTimestamp) }.collectAsLazyPagingItems()
            ChartColumn {
                items(artistsByPlayCount.itemCount) { i ->
                    val entry = artistsByPlayCount[i]
                    entry?.let {
                        ChartEntry(
                            number = i + 1,
                            label = entry.artist,
                            metricAsString = "${entry.metric} plays",
                            metric = entry.metric.toDouble() / artistsByPlayCount[0]!!.metric,
                            onClick = { bottomSheetState.showArtist(entry.artist) }
                        )
                    }
                }
            }
        }

        if (chartType == ChartType.ALBUMS && chartSort == ChartSort.PLAYS) {
            val albumsByPlayCount = remember { viewModel.albumsByPlayCount(startTimestamp, endTimestamp) }.collectAsLazyPagingItems()
            ChartColumn {
                items(albumsByPlayCount.itemCount) { i ->
                    val entry = albumsByPlayCount[i]
                    entry?.let {
                        ChartEntry(
                            number = i + 1,
                            label = entry.album,
                            label2 = entry.artist,
                            metricAsString = "${entry.metric} plays",
                            metric = entry.metric.toDouble() / albumsByPlayCount[0]!!.metric,
                            onClick = { bottomSheetState.showAlbum(entry.artist, entry.album) }
                        )
                    }
                }
            }
        }

        if (chartType == ChartType.TRACKS && chartSort == ChartSort.PLAYS) {
            val tracksByPlayCount = remember { viewModel.tracksByPlayCount(startTimestamp, endTimestamp) }.collectAsLazyPagingItems()
            ChartColumn {
                items(tracksByPlayCount.itemCount) { i ->
                    val entry = tracksByPlayCount[i]
                    entry?.let {
                        ChartEntry(
                            number = i + 1,
                            label = entry.track,
                            label2 = entry.artist,
                            metricAsString = "${entry.metric} plays",
                            metric = entry.metric.toDouble() / tracksByPlayCount[0]!!.metric,
                            onClick = { bottomSheetState.showTrack(entry.artist, entry.track) }
                        )
                    }
                }
            }
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

@Composable
private fun ChartColumn(
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier.padding(vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        content()
    }
}

@Composable
private fun ChartEntry(
    number: Int,
    label: String,
    label2: String? = null,
    metric: Double,
    metricAsString: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    ListEntryWithImage(
        modifier = modifier
            .height(87.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(5.dp))
            .clickable(onClick = onClick)
            .padding(5.dp)
    ) {
        Column(
            modifier = Modifier.padding(end = 10.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.height(28.dp)
                .wrapContentHeight(align = Alignment.CenterVertically, unbounded = true),
                    contentAlignment = Alignment.CenterStart) {
                Text(
                    "$number. $label",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            label2?.let {
                Box(modifier = Modifier.height(20.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically, unbounded = true),
                    contentAlignment = Alignment.CenterStart) {
                    Text(
                        label2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                metricAsString,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(2.dp))
            LinearProgressIndicator(
                progress = { metric.toFloat() },
                trackColor = MaterialTheme.colorScheme.surfaceContainer,
                drawStopIndicator = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(heightDp = 87)
@Composable
private fun ChartEntryPreview() {
    TrackerTheme {
        Scaffold { innerPadding ->
            ChartEntry(1, "Album Name", "Artist Name", 0.5, "Metric",
                modifier = Modifier.padding(innerPadding))
        }
    }
}

@Preview(heightDp = 87)
@Composable
private fun ChartEntryPreviewDark() {
    TrackerTheme(darkTheme = true) {
        Scaffold { innerPadding ->
            ChartEntry(1, "Album Name", "Artist Name", 0.5, "Metric",
                modifier = Modifier.padding(innerPadding))
        }
    }
}

@Preview(heightDp = 87)
@Composable
private fun ChartEntryPreview2() {
    TrackerTheme {
        Scaffold { innerPadding ->
            ChartEntry(1, "Album Name", null,  0.5, "Metric",
                modifier = Modifier.padding(innerPadding))
        }
    }
}

@Preview(heightDp = 87)
@Composable
private fun ChartEntryPreviewDark2() {
    TrackerTheme(darkTheme = true) {
        Scaffold { innerPadding ->
            ChartEntry(1, "Album Name", null, 0.5, "Metric",
                modifier = Modifier.padding(innerPadding))
        }
    }
}
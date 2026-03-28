package com.reas.tracker2.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.reas.tracker2.startKoinMp
import com.reas.tracker2.ui.navigation.ApplicationState
import com.reas.tracker2.ui.navigation.BottomSheetInfo
import com.reas.tracker2.ui.theme.TrackerTheme
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.min

// TODO kill it with fire
data class ChartEntryUiState(
    val label: String,
    val label2: String?,
    val key: String,
    val metric: Double,
    val metricAsString: String,
    val bottomSheetInfo: BottomSheetInfo,
    val url: suspend () -> Any? = { null }
)

@Composable
fun DoubleChartColumn(
    sortedByTime: Boolean,
    itemsByTime: LazyPagingItems<ChartEntryUiState>,
    itemsByPlays: LazyPagingItems<ChartEntryUiState>,
    limit: Int,
    onClick: (ChartEntryUiState) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box {
        AnimatedVisibility(
            sortedByTime,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ChartColumn(itemsByTime, limit, onClick, modifier)
        }
        AnimatedVisibility(
            !sortedByTime,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ChartColumn(itemsByPlays, limit, onClick, modifier)
        }
    }
}

@Composable
fun LazyDoubleChartColumn(
    applicationState: ApplicationState,
    sortedByTime: Boolean,
    itemsByTime: LazyPagingItems<ChartEntryUiState>,
    itemsByPlays: LazyPagingItems<ChartEntryUiState>,
    onClick: (ChartEntryUiState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state1 = rememberLazyListState()
    val state2 = rememberLazyListState()

    Box {
        AnimatedVisibility(
            sortedByTime,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyChartColumn(applicationState, itemsByTime, onClick, modifier, state1)

            LaunchedEffect(state1) {
                snapshotFlow {
                    state1.firstVisibleItemIndex to state1.firstVisibleItemScrollOffset
                }.collectLatest { (index, offset) ->
                    state2.requestScrollToItem(index, offset)
                }
            }
        }
        AnimatedVisibility(
            !sortedByTime,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyChartColumn(applicationState, itemsByPlays, onClick, modifier, state2)

            LaunchedEffect(state2) {
                snapshotFlow {
                    state2.firstVisibleItemIndex to state2.firstVisibleItemScrollOffset
                }.collectLatest { (index, offset) ->
                    state1.requestScrollToItem(index, offset)
                }
            }
        }
    }
}

@Composable
fun ChartColumn(
    items: LazyPagingItems<ChartEntryUiState>,
    limit: Int,
    onClick: (ChartEntryUiState) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(min(limit, items.itemCount)) { i ->
            val entry = items[i]
            entry?.let {
                ChartEntry(
                    number = i + 1,
                    label = entry.label,
                    label2 = entry.label2,
                    metricAsString = entry.metricAsString,
                    metric = entry.metric / items[0]!!.metric,
                    onClick = { onClick(entry) }
                )
            }
        }
    }
}

@Composable
fun LazyChartColumn(
    applicationState: ApplicationState,
    items: LazyPagingItems<ChartEntryUiState>,
    onClick: (ChartEntryUiState) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState()
) {
    LazyColumnWithScrollButton(
        applicationState = applicationState,
        modifier = modifier,
        state = state,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            items.itemCount,
            key = items.itemKey { it.key },
        ) { i ->
            val entry = items[i]
            entry?.let {
                ChartEntry(
                    number = i + 1,
                    label = entry.label,
                    label2 = entry.label2,
                    metricAsString = entry.metricAsString,
                    metric = entry.metric / items[0]!!.metric,
                    onClick = { onClick(entry) },
                    url = entry.url
                )
            }
        }
    }
}


@Composable
fun ChartEntry(
    number: Int,
    label: String,
    label2: String?,
    metric: Double,
    metricAsString: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    url: suspend () -> Any? = { null }
) {
    ListEntryWithImage(
        dynamicColor = true,
        modifier = modifier
            .height(87.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        url = url
    ) {
        Column(
            modifier = Modifier.padding(end = 10.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            ConstrainedText(
                "$number. $label",
                height = 28.dp,
                baselineHeight = 22.dp,
                style = MaterialTheme.typography.titleLarge
            )
            label2?.let {
                ConstrainedText(
                    label2,
                    height = 20.dp,
                    baselineHeight = 16.dp,
                    style = MaterialTheme.typography.titleSmall
                )
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
                trackColor = Color.Transparent,
                drawStopIndicator = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}


@Preview(heightDp = 87)
@Composable
private fun ChartEntryPreview() {
    startKoinMp {}
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
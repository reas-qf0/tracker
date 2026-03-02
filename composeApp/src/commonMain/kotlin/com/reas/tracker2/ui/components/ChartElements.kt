package com.reas.tracker2.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.reas.tracker2.database.ArtistWithTimePlayed
import com.reas.tracker2.ui.navigation.BottomSheetInfo
import com.reas.tracker2.ui.theme.TrackerTheme
import com.reas.tracker2.util.DateTimeFormatter.timeMsToString
import org.koin.core.time.inMs
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds


data class ChartEntryUiState(
    val label: String,
    val label2: String?,
    val metric: Double,
    val metricAsString: String,
    val bottomSheetInfo: BottomSheetInfo,
    val url: suspend () -> Any? = { null }
)

@Composable
fun ChartColumn(
    items: LazyPagingItems<ChartEntryUiState>,
    onClick: (ChartEntryUiState) -> Unit,
    modifier: Modifier = Modifier,
    limit: Int? = null,
) {
    if (limit != null) {
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
    } else {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items.itemCount) { i ->
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
}


@Composable
fun ChartEntry(
    number: Int,
    label: String,
    label2: String? = null,
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
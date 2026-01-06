package org.reas.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import org.reas.tracker.ui.theme.TrackerTheme

@Composable
fun<T : Any> ChartColumn(
    items: LazyPagingItems<T>,
    modifier: Modifier = Modifier,
    content: @Composable (Int, T) -> Unit,
) {
    LazyColumn(
        modifier = modifier.padding(vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items.itemCount) { i ->
            val entry = items[i]
            entry?.let {
                content(i + 1, entry)
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
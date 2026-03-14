package com.reas.tracker2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Brush.Companion.horizontalGradient
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.reas.tracker2.ui.theme.TrackerTheme
import com.reas.tracker2.ui.theme.Typography
import org.jetbrains.compose.resources.stringResource
import tracker2.composeapp.generated.resources.Res
import tracker2.composeapp.generated.resources.now_playing
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryEntry(
    title: String,
    artist: String,
    album: String?,
    timestamp: Instant,
    modifier: Modifier = Modifier,
    isNowPlaying: Boolean = false,
    imageUrl: suspend () -> Any? = { null },
    onClick: () -> Unit = {},
    onMore: () -> Unit = {},
) {
    val bgColor = if (isNowPlaying)
        MaterialTheme.colorScheme.surfaceContainerHighest
    else
        MaterialTheme.colorScheme.surfaceContainer

    ListEntryWithImage(
        backgroundColor = bgColor,
        dynamicColor = true,
        url = imageUrl,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ConstrainedText(
                    title,
                    height = 28.dp,
                    baselineHeight = 22.5.dp,
                    style = Typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1.0F)
                )
                Spacer(Modifier.width(5.dp))
                Icon(
                    Icons.Filled.MoreVert,
                    "More",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onMore)
                )
            }
            Spacer(Modifier.height(1.dp))
            Row {
                Column(modifier = Modifier.weight(1.0F)) {
                    ConstrainedText(
                        artist,
                        height = 21.dp,
                        baselineHeight = 17.dp,
                        style = Typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    album?.let {
                        Spacer(Modifier.height(1.dp))
                        ConstrainedText(
                            album,
                            height = 18.dp,
                            baselineHeight = 16.dp,
                            style = Typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
                Spacer(Modifier.width(5.dp))
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 3.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (isNowPlaying)
                        Icon(
                            Icons.Filled.PlayArrow,
                            stringResource(Res.string.now_playing),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    else {
                        Timestamp(
                            timestamp,
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryEndIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth().background(
            brush = Brush.composite(
                verticalGradient(colors = listOf(
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.surfaceContainerHighest
                )),
                horizontalGradient(
                    0.0f to Color.White.copy(alpha = 0.0f),
                    0.2f to Color.White,
                    0.8f to Color.White,
                    1.0f to Color.White.copy(alpha = 0.0f)
                ),
                blendMode = BlendMode.Modulate
            )
        )
    )
}


@Preview(widthDp = 500, heightDp = 84)
@Composable
private fun HistoryEntryPreview() {
    TrackerTheme {
        Scaffold { innerPadding ->
            HistoryEntry(
                title = "Really Long Track Name 00000000000000000000000",
                artist = "Really Long Artist Name 00000000000000000000000",
                album = "Really Long album Name 00000000000000000000000",
                timestamp = Clock.System.now(),
                isNowPlaying = false,

                onClick = {},
                onMore = {},
                imageUrl = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Preview(widthDp = 500, heightDp = 84)
@Composable
private fun HistoryEntryPreviewDark() {
    TrackerTheme(darkTheme = true) {
        Scaffold { innerPadding ->
            HistoryEntry(
                title = "Really Long Track Name 00000000000000000000000",
                artist = "Really Long Artist Name 00000000000000000000000",
                album = "Really Long album Name 00000000000000000000000",
                timestamp = Clock.System.now(),
                isNowPlaying = false,

                onClick = {},
                onMore = {},
                imageUrl = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Preview(widthDp = 500, heightDp = 84)
@Composable
private fun HistoryEntryNoAlbumPreview() {
    TrackerTheme {
        Scaffold { innerPadding ->
            HistoryEntry(
                title = "Really Long Track Name 00000000000000000000000",
                artist = "Really Long Artist Name 00000000000000000000000",
                album = null,
                timestamp = Clock.System.now(),
                isNowPlaying = false,

                onClick = {},
                onMore = {},
                imageUrl = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Preview(widthDp = 500, heightDp = 84)
@Composable
private fun HistoryEntryNoAlbumPreviewDark() {
    TrackerTheme(darkTheme = true) {
        Scaffold { innerPadding ->
            HistoryEntry(
                title = "Really Long Track Name 00000000000000000000000",
                artist = "Really Long Artist Name 00000000000000000000000",
                album = null,
                timestamp = Clock.System.now(),
                isNowPlaying = false,

                onClick = {},
                onMore = {},
                imageUrl = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Preview(widthDp = 500, heightDp = 84)
@Composable
private fun HistoryEntryNpPreview() {
    TrackerTheme {
        Scaffold { innerPadding ->
            HistoryEntry(
                title = "Really Long Track Name 00000000000000000000000",
                artist = "Really Long Artist Name 00000000000000000000000",
                album = "Really Long album Name 00000000000000000000000",
                timestamp = Clock.System.now(),
                isNowPlaying = true,

                onClick = {},
                onMore = {},
                imageUrl = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Preview(widthDp = 500, heightDp = 84)
@Composable
private fun HistoryEntryNpPreviewDark() {
    TrackerTheme(darkTheme = true) {
        Scaffold { innerPadding ->
            HistoryEntry(
                title = "Really Long Track Name 00000000000000000000000",
                artist = "Really Long Artist Name 00000000000000000000000",
                album = "Really Long album Name 00000000000000000000000",
                timestamp = Clock.System.now(),
                isNowPlaying = true,

                onClick = {},
                onMore = {},
                imageUrl = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
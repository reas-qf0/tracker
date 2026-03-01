package com.reas.tracker2.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reas.tracker2.ui.theme.Typography
import org.jetbrains.compose.resources.stringResource
import tracker2.composeapp.generated.resources.Res
import tracker2.composeapp.generated.resources.now_playing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryEntry(
    title: String,
    artist: String,
    album: String?,
    timestamp: Long,
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
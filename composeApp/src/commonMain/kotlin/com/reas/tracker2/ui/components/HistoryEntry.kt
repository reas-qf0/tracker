package com.reas.tracker2.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reas.tracker2.platform.IS_DEBUG
import com.reas.tracker2.shared.Play
import com.reas.tracker2.ui.state
import com.reas.tracker2.ui.theme.Typography
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import tracker2.composeapp.generated.resources.Res
import tracker2.composeapp.generated.resources.now_playing
import tracker2.composeapp.generated.resources.wrench

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryEntry(
    play: Play,
    modifier: Modifier = Modifier,
    imageUrl: suspend () -> Any? = { null },
    onClick: () -> Unit = {},
    onMore: () -> Unit = {},
) {
    val bgColor = if (play.isNowPlaying)
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
                    play.track,
                    height = 28.dp,
                    baselineHeight = 22.5.dp,
                    style = Typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1.0F)
                )
                Spacer(Modifier.width(5.dp))

                if (IS_DEBUG && play.associatedEvents.isNotEmpty()) {
                    var debugInfoExpanded by state(false)
                    Icon(
                        vectorResource(Res.drawable.wrench),
                        "Debug",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(
                            onClick = { debugInfoExpanded = !debugInfoExpanded }
                        )
                    )
                    DropdownMenu(
                        debugInfoExpanded,
                        onDismissRequest = { debugInfoExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Time played: ${play.timePlayed}") },
                            onClick = {}
                        )
                        DropdownMenuItem(
                            text = { Text("Associated events: ") },
                            onClick = {}
                        )
                        play.associatedEvents.forEach { event ->
                            DropdownMenuItem(
                                text = { Text(
                                    """
                                        ${event.timestamp}
                                            ${event.position}
                                            ${event.state}
                                    """.trimIndent()
                                ) },
                                onClick = {}
                            )
                        }
                    }
                }

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
                        play.artistsAsString,
                        height = 21.dp,
                        baselineHeight = 17.dp,
                        style = Typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    play.asAlbumOrNull?.let {
                        Spacer(Modifier.height(1.dp))
                        ConstrainedText(
                            it.name,
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
                    if (play.isNowPlaying)
                        Icon(
                            Icons.Filled.PlayArrow,
                            stringResource(Res.string.now_playing),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    else {
                        Timestamp(
                            play.timestamp,
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}
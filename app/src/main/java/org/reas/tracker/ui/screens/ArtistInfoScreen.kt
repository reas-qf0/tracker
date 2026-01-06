package org.reas.tracker.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.reas.tracker.ui.components.ListEntryWithImage
import org.reas.tracker.ui.components.SortOrderSelectionChip
import org.reas.tracker.ui.navigation.ArtistInfo

@Composable
fun ArtistInfoScreen(
    arguments: ArtistInfo,
    navigateToArtist: (ArtistInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val artist = arguments.artist
    val sort = arguments.sort

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.padding(5.dp)
    ) {
        ListEntryWithImage(
            modifier = Modifier.height(125.dp),
            alignment = Alignment.CenterVertically
        ) {
            Text(
                "Artist",
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1.0F)
            )
        }
        Spacer(Modifier.height(5.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1.0F)
                    .border(1.dp, Color.Gray, MaterialTheme.shapes.medium)
                    .aspectRatio(1.0F),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("1587", style = MaterialTheme.typography.headlineSmall)
                    Text("plays", color = MaterialTheme.colorScheme.secondary)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1.0F)
                    .border(1.dp, Color.Gray, MaterialTheme.shapes.medium)
                    .aspectRatio(1.0F),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("6d 00:20",
                        style = MaterialTheme.typography.headlineSmall,
                        autoSize = TextAutoSize.StepBased(
                            maxFontSize = MaterialTheme.typography.headlineSmall.fontSize
                        ),
                        maxLines = 1
                    )
                    Text("time played", color = MaterialTheme.colorScheme.secondary)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1.0F)
                    .border(1.dp, Color.Gray, MaterialTheme.shapes.medium)
                    .aspectRatio(1.0F),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("#69",
                        style = MaterialTheme.typography.headlineSmall,
                        autoSize = TextAutoSize.StepBased(
                            maxFontSize = MaterialTheme.typography.headlineSmall.fontSize
                        ),
                        maxLines = 1
                    )
                    Text("in charts", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        Spacer(Modifier.height(5.dp))
        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Top albums", style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 10.dp))
            Spacer(Modifier.weight(1.0F))
            SortOrderSelectionChip(sort, { navigateToArtist(arguments.copy(sort = it)) })
        }
    }
}
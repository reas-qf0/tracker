package org.reas.tracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.reas.tracker.ui.navigation.ChartSort

@Composable
fun SortOrderSelectionChip(
    value: ChartSort,
    setValue: (ChartSort) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        AssistChip(
            onClick = { setValue(
                if (value == ChartSort.TIME) ChartSort.PLAYS else ChartSort.TIME
            ) },
            label = { Text(value.label) },
            leadingIcon = {
                Icon(
                    value.icon,
                    contentDescription = value.label,
                    Modifier.size(AssistChipDefaults.IconSize)
                )
            }
        )
    }
}
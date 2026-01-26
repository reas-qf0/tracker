package com.reas.tracker2.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.reas.tracker2.ui.components.ChartColumn
import com.reas.tracker2.ui.components.ChartTypeSelectionChip
import com.reas.tracker2.ui.components.SortOrderSelectionChip
import com.reas.tracker2.ui.navigation.BottomSheetInfo
import com.reas.tracker2.ui.navigation.Charts
import com.reas.tracker2.ui.viewmodels.ChartsScreenViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChartsScreen(
    arguments: Charts,
    navigateToBottomSheet: (BottomSheetInfo) -> Unit,
    navigateToCharts: (Charts) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChartsScreenViewModel = koinViewModel()
) {
    val chartType = arguments.type
    val chartSort = arguments.sort

    val info = remember { viewModel.getInfo(arguments) }.collectAsLazyPagingItems()

    Column(modifier = modifier.padding(horizontal = 5.dp)) {
        Row {
            ChartTypeSelectionChip(chartType, { navigateToCharts(arguments.copy(type = it)) })
            Spacer(Modifier.width(10.dp))
            SortOrderSelectionChip(chartSort, { navigateToCharts(arguments.copy(sort = it)) })
        }

        ChartColumn(
            info,
            onClick = { entry -> navigateToBottomSheet(entry.bottomSheetInfo) },
            modifier = Modifier.padding(vertical = 5.dp)
        )
    }
}
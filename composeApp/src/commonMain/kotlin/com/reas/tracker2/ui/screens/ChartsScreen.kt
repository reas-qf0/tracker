package com.reas.tracker2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.reas.tracker2.ui.components.ChartColumn
import com.reas.tracker2.ui.components.ChartTypeSelectionChip
import com.reas.tracker2.ui.components.SortOrderSelectionChip
import com.reas.tracker2.ui.navigation.ApplicationState
import com.reas.tracker2.ui.navigation.Charts
import com.reas.tracker2.ui.viewmodels.ChartsScreenViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tracker2.composeapp.generated.resources.Res
import tracker2.composeapp.generated.resources.charts

@Composable
fun ChartsScreen(
    arguments: Charts,
    applicationState: ApplicationState,
    modifier: Modifier = Modifier,
    viewModel: ChartsScreenViewModel = koinViewModel()
) {
    val chartType = arguments.type
    val chartSort = arguments.sort

    val info = remember { viewModel.getInfo(arguments) }.collectAsLazyPagingItems()

    applicationState.setTitle(stringResource(Res.string.charts))
    Column(modifier = modifier.padding(horizontal = 5.dp)) {
        Row {
            ChartTypeSelectionChip(chartType, { applicationState.navigate(arguments.copy(type = it)) })
            Spacer(Modifier.width(10.dp))
            SortOrderSelectionChip(chartSort, { applicationState.navigate(arguments.copy(sort = it)) })
        }

        ChartColumn(
            info,
            onClick = { entry -> applicationState.navigate(entry.bottomSheetInfo) },
            modifier = Modifier.padding(vertical = 5.dp)
        )
    }
}
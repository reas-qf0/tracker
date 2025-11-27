package org.reas.tracker.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowSize
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import org.reas.tracker.R

private val WINDOW_WIDTH_LARGE = 800.dp

@Composable
fun TrackerNavigationPane(
    controller: NavHostController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val entry by controller.currentBackStackEntryAsState()
    val currentTab = entry?.destination?.route?.let {
        if (it.startsWith("history"))
            0
        else if (it.startsWith("charts"))
            1
        else if (it.startsWith("settings"))
            2
        else -1
    } ?: 0

    val windowSize = with(LocalDensity.current) {
        currentWindowSize().toSize().toDpSize()
    }
    val navLayoutType = if (windowSize.width >= WINDOW_WIDTH_LARGE) {
        // Show a permanent drawer when window width is large.
        NavigationSuiteType.NavigationDrawer
    } else {
        // Otherwise use the default from NavigationSuiteScaffold.
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
    }

    NavigationSuiteScaffold(
        navigationItems = {
            NavigationSuiteItem(
                selected = currentTab == 0,
                onClick = {
                    controller.navigate("history")
                },
                icon = { Icon(Icons.Filled.History,
                    stringResource(R.string.history)
                ) },
                label = { Text(stringResource(R.string.history)) }
            )
            NavigationSuiteItem(
                selected = currentTab == 1,
                onClick = {
                    controller.navigate("charts")
                },
                icon = { Icon(Icons.Filled.Album,
                    stringResource(R.string.charts)
                ) },
                label = { Text(stringResource(R.string.charts)) }
            )
            NavigationSuiteItem(
                selected = currentTab == 2,
                onClick = {
                    controller.navigate("settings")
                },
                icon = { Icon(Icons.Filled.Settings,
                    stringResource(R.string.settings)
                ) },
                label = { Text(stringResource(R.string.settings)) }
            )
        },
        navigationSuiteType = navLayoutType,
        modifier = modifier
    ) {
        content()
    }
}
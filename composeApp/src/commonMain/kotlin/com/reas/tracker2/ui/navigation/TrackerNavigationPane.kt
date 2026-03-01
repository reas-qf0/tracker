package com.reas.tracker2.ui.navigation

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import org.jetbrains.compose.resources.stringResource
import tracker2.composeapp.generated.resources.Res
import tracker2.composeapp.generated.resources.charts
import tracker2.composeapp.generated.resources.history
import tracker2.composeapp.generated.resources.settings

private val WINDOW_WIDTH_LARGE = 800.dp

inline fun<reified T : Any> NavBackStackEntry?.hasRoute() =
    this?.destination?.hasRoute<T>() == true

@Composable
fun TrackerNavigationPane(
    controller: NavHostController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val entry by controller.currentBackStackEntryAsState()
    val currentTab = when {
        entry.hasRoute<History>() -> 0
        entry.hasRoute<TrackHistory>() -> 0
        entry.hasRoute<Charts>() -> 1
        entry.hasRoute<ArtistInfo>() -> 1
        entry.hasRoute<AlbumInfo>() -> 1
        entry.hasRoute<TrackInfo>() -> 1
        entry.hasRoute<Settings>() -> 2
        else -> -1
    }

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
                    controller.navigate(History)
                },
                icon = { Icon(Icons.Filled.History,
                    stringResource(Res.string.history)
                ) },
                label = { Text(stringResource(Res.string.history)) }
            )
            NavigationSuiteItem(
                selected = currentTab == 1,
                onClick = {
                    controller.navigate(Charts())
                },
                icon = { Icon(Icons.Filled.Album,
                    stringResource(Res.string.charts)
                ) },
                label = { Text(stringResource(Res.string.charts)) }
            )
            NavigationSuiteItem(
                selected = currentTab == 2,
                onClick = {
                    controller.navigate(Settings)
                },
                icon = { Icon(Icons.Filled.Settings,
                    stringResource(Res.string.settings)
                ) },
                label = { Text(stringResource(Res.string.settings)) }
            )
        },
        navigationSuiteType = navLayoutType,
        modifier = modifier
    ) {
        content()
    }
}
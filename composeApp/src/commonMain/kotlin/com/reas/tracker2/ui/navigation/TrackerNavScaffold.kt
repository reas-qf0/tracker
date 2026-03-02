package com.reas.tracker2.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import com.reas.tracker2.ui.dialogs.InfoBottomSheet
import com.reas.tracker2.ui.screens.AlbumInfoScreen
import com.reas.tracker2.ui.screens.ArtistInfoScreen
import com.reas.tracker2.ui.screens.ChartsScreen
import com.reas.tracker2.ui.screens.HistoryScreen
import com.reas.tracker2.ui.screens.SettingsScreen
import com.reas.tracker2.ui.screens.TrackHistoryScreen
import com.reas.tracker2.ui.screens.TrackInfoScreen
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.stringResource
import tracker2.composeapp.generated.resources.Res
import tracker2.composeapp.generated.resources.charts
import tracker2.composeapp.generated.resources.history
import tracker2.composeapp.generated.resources.settings

class NavigationState(
    val startRoute: Route,
    val backStack: NavBackStack<Route>
) {
    init {
        backStack.add(startRoute)
    }

    @Composable
    fun toEntries(
        entryProvider: (NavKey) -> NavEntry<NavKey>
    ): SnapshotStateList<NavEntry<NavKey>> {
        val decorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
        )
        val decoratedEntries = rememberDecoratedNavEntries(
            backStack = backStack,
            entryDecorators = decorators,
            entryProvider = entryProvider
        )
        return decoratedEntries.toMutableStateList()
    }
}

@Composable
fun rememberNavigationState(
    startRoute: Route
): NavigationState {
    val backStack = rememberSerializable(serializer = serializer()) { NavBackStack<Route>() }

    return remember(startRoute) {
        NavigationState(
            startRoute = startRoute,
            backStack = backStack
        )
    }
}

class Navigator(val state: NavigationState) {
    fun navigate(route: Route) {
        if (state.backStack.last() is DialogRoute) {
            state.backStack.removeLastOrNull()
        }
        state.backStack.add(route)
    }

    fun goBack(){
        state.backStack.removeLastOrNull()
    }

    fun canNavigateBack() =
        state.backStack.filter { it !is DialogRoute }.size > 1

    fun currentTab(): Int =
        when (state.backStack.last { it !is DialogRoute }) {
            History -> 0
            is TrackHistory -> 0
            is AlbumInfo -> 1
            is ArtistInfo -> 1
            is Charts -> 1
            is TrackInfo -> 1
            Settings -> 2
            else -> -1
        }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerNavScaffold(
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }

    val navigationState = rememberNavigationState(startRoute = History)
    val controller = remember { Navigator(navigationState) }
    val canNavigateBack by remember { derivedStateOf { controller.canNavigateBack() } }
    val currentTab by remember { derivedStateOf { controller.currentTab() } }

    val navLayoutType = NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfo())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        title,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    ) },
                navigationIcon = {
                    IconButton(
                        onClick = { controller.goBack() },
                        enabled = canNavigateBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(),
        modifier = modifier
    ) { innerPadding ->
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
            navigationItemVerticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            modifier = Modifier.padding(innerPadding)
        ) {
            val entryProvider = entryProvider<NavKey> {
                entry<History> {
                    title = stringResource(Res.string.history)
                    HistoryScreen(
                        navigateToBottomSheet = { controller.navigate(it) },
                    )
                }

                entry<TrackHistory> { arguments ->
                    title = "${arguments.track.artist} - ${arguments.track._track}"
                    TrackHistoryScreen(
                        arguments = arguments,
                        navigateToBottomSheet = { controller.navigate(it) },
                    )
                }

                entry<Charts> { arguments ->
                    title = stringResource(Res.string.charts)
                    ChartsScreen(
                        arguments = arguments,
                        navigateToBottomSheet = { controller.navigate(it) },
                        navigateToCharts = { controller.navigate(it) }
                    )
                }

                entry<Settings> {
                    title = stringResource(Res.string.settings)
                    SettingsScreen()
                }

                entry<ArtistInfo> { arguments ->
                    title = arguments.artist
                    ArtistInfoScreen(
                        arguments = arguments,
                        navigateToArtist = { controller.navigate(it) },
                        navigateToBottomSheet = { controller.navigate(it) }
                    )
                }

                entry<AlbumInfo> { arguments ->
                    title = "${arguments.album.artist} - ${arguments.album.title}"
                    AlbumInfoScreen(
                        arguments = arguments,
                        navigateToAlbum = { controller.navigate(it) },
                        navigateToBottomSheet = { controller.navigate(it) }
                    )
                }

                entry<TrackInfo> { arguments ->
                    title = "${arguments.track.artist} - ${arguments.track._track}"
                    TrackInfoScreen(
                        arguments = arguments,
                        navigateToTrack = { controller.navigate(it) },
                        navigateToBottomSheet = { controller.navigate(it) }
                    )
                }

                entry<BottomSheetInfo>(metadata = DialogSceneStrategy.dialog()) { arguments ->
                    InfoBottomSheet(
                        arguments = arguments,
                        onDismiss = { controller.goBack() },
                        navigateToArtist = { controller.navigate(it) },
                        navigateToAlbum = { controller.navigate(it) },
                        navigateToTrack = { controller.navigate(it) },
                        navigateToTrackHistory = { controller.navigate(it) }
                    )
                }
            }

            NavDisplay(
                entries = navigationState.toEntries(entryProvider),
                onBack = { controller.goBack() },
                sceneStrategy = remember { DialogSceneStrategy() }
            )
        }
    }
}
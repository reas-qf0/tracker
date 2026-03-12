package com.reas.tracker2.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.reas.tracker2.ui.dialogs.InfoBottomSheet
import com.reas.tracker2.ui.navigation.*
import com.reas.tracker2.ui.screens.*
import com.reas.tracker2.ui.theme.TrackerTheme
import org.jetbrains.compose.resources.stringResource
import tracker2.composeapp.generated.resources.Res
import tracker2.composeapp.generated.resources.charts
import tracker2.composeapp.generated.resources.history
import tracker2.composeapp.generated.resources.settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerApp(
    modifier: Modifier = Modifier,
) {
    val applicationState = rememberApplicationState(startRoute = History)

    TrackerBackgroundProcesses()
    TrackerTheme {
        TrackerNavScaffold(
            applicationState, modifier,
            navigationItems = listOf(
                TrackerNavItem(
                    title = stringResource(Res.string.history),
                    icon = Icons.Filled.History,
                    destination = History
                ),
                TrackerNavItem(
                    title = stringResource(Res.string.charts),
                    icon = Icons.Filled.Album,
                    destination = Charts()
                ),
                TrackerNavItem(
                    title = stringResource(Res.string.settings),
                    icon = Icons.Filled.Settings,
                    destination = Settings
                )
            )
        ) {
            entry<History> {
                HistoryScreen(applicationState)
            }

            entry<TrackHistory> { arguments ->
                TrackHistoryScreen(arguments, applicationState)
            }

            entry<Charts> { arguments ->
                ChartsScreen(arguments, applicationState)
            }

            entry<Settings> {
                SettingsScreen(applicationState)
            }

            entry<ArtistInfo> { arguments ->
                ArtistInfoScreen(arguments, applicationState)
            }

            entry<AlbumInfo> { arguments ->
                AlbumInfoScreen(arguments, applicationState)
            }

            entry<TrackInfo> { arguments ->
                TrackInfoScreen(arguments, applicationState)
            }

            dialog<BottomSheetInfo> { arguments ->
                InfoBottomSheet(arguments, applicationState)
            }
        }
    }
}
package com.reas.tracker2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.reas.tracker2.ui.dialogs.InfoBottomSheet
import com.reas.tracker2.ui.screens.AlbumInfoScreen
import com.reas.tracker2.ui.screens.ArtistInfoScreen
import com.reas.tracker2.ui.screens.ChartsScreen
import com.reas.tracker2.ui.screens.HistoryScreen
import com.reas.tracker2.ui.screens.SettingsScreen
import com.reas.tracker2.ui.screens.TrackHistoryScreen
import com.reas.tracker2.ui.screens.TrackInfoScreen
import org.jetbrains.compose.resources.stringResource
import tracker2.composeapp.generated.resources.Res
import tracker2.composeapp.generated.resources.charts
import tracker2.composeapp.generated.resources.history
import tracker2.composeapp.generated.resources.settings

@Composable
fun TrackerNavHost(
    title: MutableState<String>,
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = History
    ) {
        composable<History> {
            title.value = stringResource(Res.string.history)
            HistoryScreen(
                navigateToBottomSheet = { navController.navigate(it) },
            )
        }

        composable<TrackHistory> { backStackEntry ->
            val arguments: TrackHistory = backStackEntry.toRoute()
            title.value = "${arguments.artist} - ${arguments.track}"
            TrackHistoryScreen(
                arguments = arguments,
                navigateToBottomSheet = { navController.navigate(it) },
            )
        }

        composable<Charts> { backStackEntry ->
            val arguments: Charts = backStackEntry.toRoute()
            title.value = stringResource(Res.string.charts)
            ChartsScreen(
                arguments = arguments,
                navigateToBottomSheet = { navController.navigate(it) },
                navigateToCharts = { navController.navigate(it) }
            )
        }

        composable<Settings> {
            title.value = stringResource(Res.string.settings)
            SettingsScreen()
        }

        composable<ArtistInfo> { backStackEntry ->
            val arguments: ArtistInfo = backStackEntry.toRoute()
            title.value = arguments.artist
            ArtistInfoScreen(
                arguments = arguments,
                navigateToArtist = { navController.navigate(it) },
                navigateToBottomSheet = { navController.navigate(it) }
            )
        }

        composable<AlbumInfo> { backStackEntry ->
            val arguments: AlbumInfo = backStackEntry.toRoute()
            title.value = "${arguments.artist} - ${arguments.album}"
            AlbumInfoScreen(
                arguments = arguments,
                navigateToAlbum = { navController.navigate(it) },
                navigateToBottomSheet = { navController.navigate(it) }
            )
        }

        composable<TrackInfo> { backStackEntry ->
            val arguments: TrackInfo = backStackEntry.toRoute()
            title.value = "${arguments.artist} - ${arguments.track}"
            TrackInfoScreen(
                arguments = arguments,
                navigateToTrack = { navController.navigate(it) },
                navigateToBottomSheet = { navController.navigate(it) }
            )
        }

        dialog<BottomSheetInfo> { backStackEntry ->
            val arguments: BottomSheetInfo = backStackEntry.toRoute()
            InfoBottomSheet(
                arguments = arguments,
                onDismiss = { navController.popBackStack() },
                navigateToArtist = { navController.navigate(it) },
                navigateToAlbum = { navController.navigate(it) },
                navigateToTrack = { navController.navigate(it) },
                navigateToTrackHistory = { navController.navigate(it) }
            )
        }
    }
}
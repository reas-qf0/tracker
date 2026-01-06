package org.reas.tracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.google.firebase.auth.FirebaseUser
import org.reas.tracker.R
import org.reas.tracker.ui.dialogs.InfoBottomSheet
import org.reas.tracker.ui.screens.*

@Composable
fun TrackerNavHost(
    title: MutableState<String>,
    user: FirebaseUser,
    signOut: () -> Unit,
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = History
    ) {
        composable<History> {
            title.value = stringResource(R.string.history)
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
            title.value = stringResource(R.string.charts)
            ChartsScreen(
                arguments = arguments,
                navigateToBottomSheet = { navController.navigate(it) },
                navigateToCharts = { navController.navigate(it) }
            )
        }

        composable<Settings> {
            title.value = stringResource(R.string.settings)
            SettingsScreen(signOut = signOut)
        }

        composable<ArtistInfo> { backStackEntry ->
            val arguments: ArtistInfo = backStackEntry.toRoute()
            title.value = arguments.artist
            ArtistInfoScreen(
                arguments = arguments,
                navigateToArtist = { navController.navigate(it) }
            )
        }

        composable<AlbumInfo> { backStackEntry ->
            val arguments: AlbumInfo = backStackEntry.toRoute()
            title.value = "${arguments.artist} - ${arguments.album}"
            AlbumInfoScreen(
                arguments = arguments,
            )
        }

        composable<TrackInfo> { backStackEntry ->
            val arguments: TrackInfo = backStackEntry.toRoute()
            title.value = "${arguments.artist} - ${arguments.track}"
            TrackInfoScreen(
                arguments = arguments,
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
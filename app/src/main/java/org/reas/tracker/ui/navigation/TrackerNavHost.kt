package org.reas.tracker.ui.navigation

import android.R.attr.type
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.savedstate.SavedState
import com.google.firebase.auth.FirebaseUser
import org.reas.tracker.R
import org.reas.tracker.ui.screens.*
import kotlin.io.encoding.Base64

private fun encode(s: String) = Base64.UrlSafe.encode(s.encodeToByteArray())
private fun SavedState.decodeString(key: String) = getString(key)?.let {
    Base64.UrlSafe.decode(it).decodeToString()
}

@Composable
fun TrackerNavHost(
    title: MutableState<String>,
    user: FirebaseUser,
    signOut: () -> Unit,
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = "history"
    ) {
        composable(route = "history") {
            title.value = stringResource(R.string.history)
            HistoryScreen(
                navigateToArtist = { artist ->
                    val artistE = encode(artist)
                    navController.navigate("charts/artist/$artistE")
                },
                navigateToAlbum = { artist, album ->
                    val artistE = encode(artist)
                    val albumE = encode(album)
                    navController.navigate("charts/album/$artistE/$albumE")
                },
                navigateToTrack = { artist, track, album ->
                    val artistE = encode(artist)
                    val trackE = encode(track)
                    if (album != null) {
                        val albumE = encode(album)
                        navController.navigate("charts/track/$artistE/$trackE?album=$albumE")
                    } else {
                        navController.navigate("charts/track/$artistE/$trackE")
                    }
                },
                navigateToTrackHistory = { artist, track, album ->
                    val artistE = encode(artist)
                    val trackE = encode(track)
                    if (album != null) {
                        val albumE = encode(album)
                        navController.navigate("history/track/$artistE/$trackE?album=$albumE")
                    } else {
                        navController.navigate("history/track/$artistE/$trackE")
                    }
                }
            )
        }

        composable(
            route = "history/track/{artist}/{track}?album={album}",
            arguments = listOf(
                navArgument("artist") {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument("track") {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument("album") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val arguments = backStackEntry.arguments!!
            val artist = arguments.decodeString("artist")!!
            val track = arguments.decodeString("track")!!
            val album = arguments.decodeString("album")
            title.value = "$artist - $track"
            TrackHistoryScreen(
                artist = artist,
                track = track,
                album = album
            )
        }

        composable(route = "charts") {
            title.value = stringResource(R.string.charts)
            ChartsScreen()
        }

        composable(route = "settings") {
            title.value = stringResource(R.string.settings)
            SettingsScreen(signOut = signOut)
        }

        composable(
            route = "charts/artist/{artist}",
            arguments = listOf(
                navArgument("artist") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val arguments = backStackEntry.arguments!!
            val artist = arguments.decodeString("artist")!!
            title.value = artist
            ArtistInfoScreen(artist = artist)
        }

        composable(
            route = "charts/album/{artist}/{album}",
            arguments = listOf(
                navArgument("artist") {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument("album") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val arguments = backStackEntry.arguments!!
            val artist = arguments.decodeString("artist")!!
            val album = arguments.decodeString("album")!!
            title.value = "$artist - $album"
            AlbumInfoScreen(
                artist = artist,
                album = album
            )
        }

        composable(
            route = "charts/track/{artist}/{track}?album={album}",
            arguments = listOf(
                navArgument("artist") {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument("track") {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument("album") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val arguments = backStackEntry.arguments!!
            val artist = arguments.decodeString("artist")!!
            val track = arguments.decodeString("track")!!
            val album = arguments.decodeString("album")
            title.value = "$artist - $track"
            TrackInfoScreen(
                artist = artist,
                track = track,
                album = album
            )
        }
    }
}
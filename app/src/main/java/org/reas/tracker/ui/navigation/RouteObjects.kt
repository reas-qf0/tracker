package org.reas.tracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
enum class ChartType {
    ARTISTS {
        override val icon: ImageVector
            get() = Icons.Filled.Person
        override val label: String
            get() = "Artists"
    },
    ALBUMS {
        override val icon: ImageVector
            get() = Icons.Filled.Album
        override val label: String
            get() = "Albums"
    },
    TRACKS {
        override val icon: ImageVector
            get() = Icons.Filled.MusicNote
        override val label: String
            get() = "Tracks"
    };

    @Transient abstract val icon: ImageVector
    @Transient abstract val label: String
}


@Serializable
enum class ChartSort {
    TIME {
        override val icon: ImageVector
            get() = Icons.Filled.AccessTime
        override val label: String
            get() = "Time"
    }, PLAYS {
        override val icon: ImageVector
            get() = Icons.Filled.PlayArrow
        override val label: String
            get() = "Plays"
    };

    @Transient abstract val icon: ImageVector
    @Transient abstract val label: String
}


@Serializable
object History

@Serializable
data class TrackHistory(
    val artist: String,
    val track: String,
    val album: String? = null
)

@Serializable
data class Charts(
    val sort: ChartSort = ChartSort.TIME,
    val type: ChartType = ChartType.ARTISTS
)

@Serializable
object Settings

@Serializable
data class ArtistInfo(
    val artist: String,
    val sort: ChartSort = ChartSort.TIME
)

@Serializable
data class AlbumInfo(
    val artist: String,
    val album: String,
    val sort: ChartSort = ChartSort.TIME
)

@Serializable
data class TrackInfo(
    val artist: String,
    val album: String?,
    val track: String,
    val sort: ChartSort = ChartSort.TIME
)

@Serializable
data class BottomSheetInfo(
    val artist: String,
    val track: String? = null,
    val album: String? = null,
    val albumArtist: String? = null
)
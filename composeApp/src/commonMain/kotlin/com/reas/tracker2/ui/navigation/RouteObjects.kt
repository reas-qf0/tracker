package com.reas.tracker2.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.compose.resources.StringResource
import tracker2.composeapp.generated.resources.Res
import tracker2.composeapp.generated.resources.albums
import tracker2.composeapp.generated.resources.artists
import tracker2.composeapp.generated.resources.plays
import tracker2.composeapp.generated.resources.time_played
import tracker2.composeapp.generated.resources.tracks


@Serializable
enum class ChartType {
    ARTISTS {
        override val icon: ImageVector
            get() = Icons.Filled.Person
        override val label: StringResource
            get() = Res.string.artists
    },
    ALBUMS {
        override val icon: ImageVector
            get() = Icons.Filled.Album
        override val label: StringResource
            get() = Res.string.albums
    },
    TRACKS {
        override val icon: ImageVector
            get() = Icons.Filled.MusicNote
        override val label: StringResource
            get() = Res.string.tracks
    };

    @Transient abstract val icon: ImageVector
    @Transient abstract val label: StringResource
}


@Serializable
enum class ChartSort {
    TIME {
        override val icon: ImageVector
            get() = Icons.Filled.AccessTime
        override val label: StringResource
            get() = Res.string.time_played
    }, PLAYS {
        override val icon: ImageVector
            get() = Icons.Filled.PlayArrow
        override val label: StringResource
            get() = Res.string.plays
    };

    @Transient abstract val icon: ImageVector
    @Transient abstract val label: StringResource
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
    val sortS: String = ChartSort.TIME.name,
    val typeS: String = ChartType.ARTISTS.name
) {
    val sort: ChartSort
        get() = ChartSort.valueOf(sortS)
    val type: ChartType
        get() = ChartType.valueOf(typeS)
}

@Serializable
object Settings

@Serializable
data class ArtistInfo(
    val artist: String,
    val sortS: String = ChartSort.TIME.name
) {
    val sort: ChartSort
        get() = ChartSort.valueOf(sortS)
}

@Serializable
data class AlbumInfo(
    val artist: String,
    val album: String,
    val sortS: String = ChartSort.TIME.name
) {

    val sort: ChartSort
        get() = ChartSort.valueOf(sortS)
}

@Serializable
data class TrackInfo(
    val artist: String,
    val album: String?,
    val track: String,
    val sortS: String = ChartSort.TIME.name
) {

    val sort: ChartSort
        get() = ChartSort.valueOf(sortS)
}

@Serializable
data class BottomSheetInfo(
    val artist: String,
    val track: String? = null,
    val album: String? = null,
    val albumArtist: String? = null
)
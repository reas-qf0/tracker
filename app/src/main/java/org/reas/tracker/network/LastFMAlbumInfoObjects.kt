package org.reas.tracker.network

import com.google.gson.annotations.SerializedName

data class LastFMAlbumInfoWrapper(
    val album: LastFMAlbumInfo
)

data class LastFMAlbumInfo(
    val image: List<LastFMImageInfo>
)

data class LastFMImageInfo(
    val size: String,
    @SerializedName("#text") val url: String
)
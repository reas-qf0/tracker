package com.reas.tracker2.network

import co.touchlab.kermit.Logger
import com.reas.tracker2.shared.Album
import com.reas.tracker2.util.Secrets
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface NetworkRepository {
    suspend fun getAlbumImageUrl(album: Album, size: String): String?
}

class KtorNetworkRepository : NetworkRepository, KoinComponent {
    private val client: HttpClient by inject()

    override suspend fun getAlbumImageUrl(album: Album, size: String): String? {
        val response = client.get {
            url {
                host = "ws.audioscrobbler.com"
                path("2.0")
                parameters.apply {
                    append("method", "album.getinfo")
                    append("api_key", Secrets.LASTFM_API_KEY)
                    append("format", "json")
                    append("artist", album.artist)
                    append("album", album.title)
                }
            }
        }
        val body = response.bodyAsText()
        Logger.d("Network") { response.request.url.toString() }
        Logger.d("Network") { body }
        return response.body<LastFMAlbumInfoWrapper>().album.image.firstOrNull { it.size == size }?.url
    }
}
package com.reas.tracker2.network

import com.reas.tracker2.shared.Album
import com.reas.tracker2.shared.Event
import com.reas.tracker2.util.Secrets
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface NetworkRepository {
    suspend fun getAlbumImageUrl(album: Album, size: String): String?
    suspend fun submitEvent(event: Event): Boolean
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
        return response.body<LastFMAlbumInfoWrapper>().album.image.firstOrNull { it.size == size }?.url
    }

    override suspend fun submitEvent(event: Event): Boolean {
        try {
            val response = client.post {
                url {
                    host = "192.168.0.4"
                    port = 8080
                    url("addEvent")
                }
                contentType(ContentType.Application.Json)
                setBody(event)
            }
            return response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            return false
        }
    }
}
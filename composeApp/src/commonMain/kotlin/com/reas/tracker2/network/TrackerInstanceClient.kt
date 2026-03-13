 package com.reas.tracker2.network

import co.touchlab.kermit.Logger
import com.reas.tracker2.database.Repository
import com.reas.tracker2.shared.Event
import com.reas.tracker2.shared.Play
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

 data class SyncEvent(
    val id: Long,
    val event: Event,
)

class TrackerInstanceClient(
    private val repository: Repository,
    private val client: HttpClient,
) {
    private var host_ = ""
    private var port_ = 0
    private var apiKey = ""

    suspend fun tryLogin(hostname: String, port: Int, username: String): String? {
        host_ = hostname
        port_ = port
        repository.getKey(hostname, port, username)?.let {
            apiKey = it
            return null
        }
        try {
            val r = client.post {
                url { request ->
                    request.host = host_
                    request.port = port_
                    path("login")
                    parameters.append("user", username)
                }
            }
            if (!r.status.isSuccess()) {
                Logger.w(tag = TAG) { "Error while trying to login: ${r.status}" }
                return r.status.description
            }
            apiKey = r.bodyAsText()
            repository.addKey(hostname, port, username, apiKey)
            return null
        } catch (e: Exception) {
            Logger.w(tag = TAG, throwable = e) { "Error while trying to login" }
            return "${e.javaClass.name}${if (e.message != null) ": " + e.message else ""}"
        }
    }

    suspend fun tryEstablishConnection(): Boolean {
        try {
            Logger.d(tag = TAG) { "connecting to server" }
            client.webSocket(
                request = {
                    url {
                        //protocol = URLProtocol.WS
                        host = host_
                        port = port_
                        url("sync")
                        parameters.append("api_key", apiKey)
                    }
                }
            ) {
                Logger.d(tag = TAG) { "connected to server" }

                val receiveJob = launch {
                    while (true) {
                        val play = receiveDeserialized<Play>()
                        Logger.d(tag = TAG) { "received play from server" }
                        repository.insertPlay(play)
                    }
                }

                outgoing.invokeOnClose {
                    Logger.d(tag = TAG) { "connection closed" }
                    receiveJob.cancel()
                }

                receiveJob.join()
            }
            return true
        } catch (e: Exception) {
            Logger.w(throwable = e, tag = TAG) { "connection error" }
            return false
        }
    }

    suspend fun submitEvent(event: Event) {
        try {
            val r = client.post {
                url {
                    //protocol = URLProtocol.HTTP
                    host = host_
                    port = port_
                    path("scrobble")
                    parameters.append("api_key", apiKey)
                }
                contentType(ContentType.Application.Json)
                setBody(event)
            }
            if (!r.status.isSuccess()) {
                Logger.d(tag = TAG) { "submit failed with code ${r.status.value}; storing event" }
                repository.insertEventInSync(event)
            }
            Logger.d(tag = TAG) { "submitted event" }
            submitFromQueue()
        } catch (e: Exception) {
            Logger.w(throwable = e, tag = TAG) { "submit failed; storing event" }
            repository.insertEventInSync(event)
        }
    }

    suspend fun submitFromQueue() {
        val events = repository.getEventsInSync().first()
        if (events.isEmpty()) return
        events.chunked(500).forEach { batch ->
            Logger.d(tag = TAG) { "submitting ${batch.size} events from queue" }
            try {
                val r = client.post {
                    url {
                        //protocol = URLProtocol.HTTP
                        host = host_
                        port = port_
                        path("scrobble")
                        parameters.append("api_key", apiKey)
                    }
                    contentType(ContentType.Application.Json)
                    setBody(batch.map { it.event })
                }
                if (!r.status.isSuccess()) {
                    Logger.d(tag = TAG) { "submit failed with code ${r.status.value}; storing events" }
                } else {
                    Logger.d(tag = TAG) { "batch event submit successful" }
                    repository.deleteFromSync(batch.map { it.id })
                }
            } catch (e: Exception) {
                Logger.w(throwable = e, tag = TAG) { "submit failed; storing event" }
            }
        }
    }

    companion object {
        private const val TAG = "TrackerInstanceClient"
    }
}
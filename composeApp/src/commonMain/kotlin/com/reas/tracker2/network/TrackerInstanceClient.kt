 package com.reas.tracker2.network

import co.touchlab.kermit.Logger
import com.reas.tracker2.database.Repository
import com.reas.tracker2.settings.Settings
import com.reas.tracker2.settings.get
import com.reas.tracker2.settings.lastSeenId
import com.reas.tracker2.settings.set
import com.reas.tracker2.shared.Event
import com.reas.tracker2.shared.Play
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

 data class SyncEvent(
    val id: Long,
    val event: Event,
)

class TrackerInstanceClient(
    private val repository: Repository,
    private val client: HttpClient,
    private val settings: Settings
) {
    private var host_ = ""
    private var port_ = 0
    private var apiKey = ""

    private val queueLock = Mutex()

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
                        host = host_
                        port = port_
                        url("sync")
                        parameters.append("api_key", apiKey)
                    }
                }
            ) {
                Logger.d(tag = TAG) { "connected to server" }

                // TODO: figure out something better
                launch { submitEvents() }

                send(settings.get(lastSeenId).toString())

                val receiveJob = launch {
                    incoming.consumeEach { frame ->
                        if (frame !is Frame.Text) return@consumeEach
                        val body = frame.readText()
                        val plays = try {
                            listOf(Json.decodeFromString<Play>(body))
                        } catch (e: SerializationException) {
                            Json.decodeFromString<List<Play>>(body)
                        } catch (e: SerializationException) {
                            Logger.w(tag = TAG, throwable = e) { "couldn't deserialize plays from server" }
                            return@consumeEach
                        }

                        var expectingId = settings.get(lastSeenId) + 1
                        val playsById = plays.associateBy { it.id }
                        while (playsById.containsKey(expectingId)) {
                            expectingId++
                        }

                        settings.set(lastSeenId, expectingId - 1)
                        send(expectingId.toString())
                        repository.insertPlays(plays)
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
        // TODO: figure out something better
        repository.insertEventInSync(event)
        submitEvents()
    }

    suspend fun submitEvents() {
        queueLock.withLock {
            val events = repository.getEventsInSync().first()
            if (events.isEmpty()) return
            events.chunked(100).forEach { batch ->
                Logger.d(tag = TAG) { "submitting ${batch.size} events from queue" }
                try {
                    val r = client.post {
                        url {
                            host = host_
                            port = port_
                            path("scrobble")
                            parameters.append("api_key", apiKey)
                        }
                        contentType(ContentType.Application.Json)
                        setBody(batch.map { it.event })
                    }
                    if (!r.status.isSuccess()) {
                        Logger.d(tag = TAG) { "batch event submit failed with code ${r.status.value}" }
                        return
                    } else {
                        Logger.d(tag = TAG) { "batch event submit successful" }
                        repository.deleteFromSync(batch.map { it.id })
                    }
                } catch (e: Exception) {
                    Logger.w(throwable = e, tag = TAG) { "batch event submit failed" }
                    return
                }
            }
        }
    }

    companion object {
        private const val TAG = "TrackerInstanceClient"
    }
}
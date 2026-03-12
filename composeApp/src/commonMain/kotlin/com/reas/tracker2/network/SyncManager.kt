 package com.reas.tracker2.network

import co.touchlab.kermit.Logger
import com.reas.tracker2.database.Repository
import com.reas.tracker2.shared.Event
import com.reas.tracker2.shared.Play
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class SyncEvent(
    val id: Long,
    val event: Event,
)

class SyncManager(
    private val repository: Repository,
    private val client: HttpClient,
) {
    // TODO: replace with actual device identification
    @OptIn(ExperimentalUuidApi::class)
    private val deviceId = Uuid.random().toHexString()

    suspend fun establishConnection(): Boolean {
        try {
            Logger.d(TAG) { "connecting to server" }
            client.webSocket(
                request = {
                    url {
                        host = "192.168.0.4"
                        port = 8080
                        url("sync")
                        parameters.append("device-id", deviceId)
                    }
                }
            ) {
                Logger.d(TAG) { "connected to server" }

                val receiveJob = launch {
                    while (true) {
                        val play = receiveDeserialized<Play>()
                        Logger.d(TAG) { "received play from server" }
                        repository.insertPlay(play)
                    }
                }

                outgoing.invokeOnClose {
                    Logger.d(TAG) { "connection closed" }
                    receiveJob.cancel()
                }

                receiveJob.join()
            }
            return true
        } catch (e: Exception) {
            Logger.w(TAG, e) { "connection error" }
            return false
        }
    }

    suspend fun submitEvent(event: Event) {
        try {
            val r = client.post {
                url {
                    host = "192.168.0.4"
                    port = 8080
                    path("scrobble")
                    parameters.append("device-id", deviceId)
                }
                contentType(ContentType.Application.Json)
                setBody(event)
            }
            if (!r.status.isSuccess()) {
                Logger.d(TAG) { "submit failed with code ${r.status.value}; storing event" }
                repository.insertEventInSync(event)
            }
            Logger.d(TAG) { "submitted event" }
            submitFromQueue()
        } catch (e: Exception) {
            Logger.w(TAG, e) { "submit failed; storing event" }
            repository.insertEventInSync(event)
        }
    }

    suspend fun submitFromQueue() {
        val events = repository.getEventsInSync().first()
        if (events.isEmpty()) return
        events.chunked(500).forEach { batch ->
            Logger.d(TAG) { "submitting ${batch.size} events from queue" }
            try {
                val r = client.post {
                    url {
                        host = "192.168.0.4"
                        port = 8080
                        path("scrobble")
                        parameters.append("device-id", deviceId)
                    }
                    contentType(ContentType.Application.Json)
                    setBody(batch.map { it.event })
                }
                if (!r.status.isSuccess()) {
                    Logger.d(TAG) { "submit failed with code ${r.status.value}; storing events" }
                } else {
                    Logger.d(TAG) { "submit successful" }
                    repository.deleteFromSync(batch.map { it.id })
                }
            } catch (e: Exception) {
                Logger.w(TAG, e) { "submit failed; storing event" }
            }
        }
    }

    companion object {
        private const val TAG = "SyncManager"
    }
}
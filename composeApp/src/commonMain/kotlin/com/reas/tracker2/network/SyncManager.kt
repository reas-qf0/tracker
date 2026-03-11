package com.reas.tracker2.network

import co.touchlab.kermit.Logger
import com.reas.tracker2.database.Repository
import com.reas.tracker2.shared.Event
import com.reas.tracker2.shared.Play
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SyncManager(
    private val repository: Repository,
    private val client: HttpClient,
) {
    private var connection: DefaultClientWebSocketSession? = null
    private val outgoingEvents = MutableSharedFlow<Event>()
    private val mutex = Mutex()

    // TODO: replace with actual device identification
    @OptIn(ExperimentalUuidApi::class)
    private val deviceId = Uuid.random().toHexString()

    suspend fun establishConnection() {
        try {
            while (true) {
                if (connection != null) return
                mutex.lock()
                //Logger.d(TAG) { "mutex.lock" }
                if (connection != null) {
                    mutex.unlock()
                    //Logger.d(TAG) { "mutex.unlock" }
                    return
                }
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
                    connection = this
                    mutex.unlock()
                    //Logger.d(TAG) { "mutex.unlock" }

                    val sendJob = launch {
                        outgoingEvents.collect { event ->
                            Logger.d(TAG) { "sending event" }
                            sendSerialized(event)
                        }
                    }

                    val receiveJob = launch {
                        submitFromQueue()
                        while (true) {
                            val play = receiveDeserialized<Play>()
                            Logger.d(TAG) { "received play from server" }
                            repository.insertPlay(play)
                        }
                    }

                    outgoing.invokeOnClose {
                        connection = null
                        Logger.d(TAG) { "connection closed" }
                        receiveJob.cancel()
                        sendJob.cancel()
                    }

                    receiveJob.join()
                    sendJob.join()
                }
                Logger.d(TAG) { "waiting 15 seconds before reconnecting" }
                delay(15.seconds)
            }
        } catch (e: Exception) {
            Logger.w(TAG, e) { "connection error" }
            mutex.unlock()
        }
    }

    suspend fun addEvent(event: Event) {
        if (connection == null) {
            Logger.d(TAG) { "no connection; storing event & trying to reconnect" }
            repository.insertEventInSync(event)
            establishConnection()
        } else {
            outgoingEvents.emit(event)
        }
    }

    suspend fun submitFromQueue() {
        val events = repository.getEventsInSync().first()
        if (events.isEmpty()) return
        repository.clearSyncQueue()
        Logger.d(TAG) { "submitting ${events.size} events from queue" }
        events.forEach { event ->
            addEvent(event)
        }
    }

    companion object {
        private const val TAG = "SyncManager"
    }
}
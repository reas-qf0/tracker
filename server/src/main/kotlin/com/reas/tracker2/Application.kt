package com.reas.tracker2

import com.reas.tracker2.database.EventProcessorAdapterImpl
import com.reas.tracker2.database.Repository
import com.reas.tracker2.shared.Event
import com.reas.tracker2.shared.EventProcessor
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::main)
        .start(wait = true)
}

fun Application.main() {
    install(Koin) {
        slf4jLogger()
        modules(module)
    }
    install(ContentNegotiation) {
        json()
    }
    install(WebSockets) {
        pingPeriod = 15.seconds
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }

    val repository: Repository by inject()
    val adapter: EventProcessorAdapterImpl by inject()
    val eventProcessor: EventProcessor by inject()

    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    scope.launch {
        eventProcessor.processQueue()
    }

    routing {
        get("/events") {
            call.respond(repository.getEvents())
        }
        get("/plays") {
            call.respond(repository.getPlays())
        }
        post("/scrobble") {
            val deviceId = call.parameters["device-id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val body = call.receiveText()
            val events = try {
                listOf(Json.decodeFromString<Event>(body))
            } catch (e: SerializationException) {
                Json.decodeFromString<List<Event>>(body)
            } catch (e: SerializationException) {
                return@post call.respond(HttpStatusCode.BadRequest)
            }
            events.forEach { event ->
                val event_ = event.copy(source = event.source.copy(device = deviceId))
                repository.insertEvent(event_)
                adapter.addEvent(event_)
            }
            call.respond(HttpStatusCode.OK)
        }
        webSocket("/sync") {
            val deviceId = call.request.queryParameters["device-id"] ?:
                return@webSocket close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Device ID not specified"))
            log.info(deviceId)
            adapter.onPlay {
                if (it.sourceDevice == deviceId) return@onPlay
                sendSerialized(it)
            }
        }
    }
}
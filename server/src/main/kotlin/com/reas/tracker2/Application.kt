package com.reas.tracker2

import com.reas.tracker2.api.EventAPI
import com.reas.tracker2.api.PlayAPI
import com.reas.tracker2.database.Repository
import com.reas.tracker2.shared.EventProcessor
import com.reas.tracker2.shared.HolePlugger
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import java.io.File
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
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
    val eventProcessor: EventProcessor by inject()
    val holePlugger: HolePlugger by inject()

    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val onUpdate = MutableSharedFlow<Unit>()
    scope.launch {
        eventProcessor.processQueue()
    }
    scope.launch {
        eventProcessor.collectPlays { plays ->
            repository.insertPlays(plays)
            holePlugger.register(plays)
            onUpdate.emit(Unit)
        }
    }
    scope.launch {
        holePlugger.collectPlays { play ->
            repository.insertPlays(listOf(play))
        }
    }

    routing {
        staticFiles("", File("server/webApp/dist"))
        get("/events") {
            call.respond(repository.getEvents().map { event ->
                EventAPI.fromEvent(event)
            })
        }
        get("/plays") {
            call.respond(repository.getPlays().map { play ->
                PlayAPI.fromPlay(play)
            })
        }
        post("/login") {
            val user = call.request.queryParameters["user"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            call.respondText(repository.registerUser(user))
        }
        post("/scrobble") {
            authorization { user ->
                val body = call.receiveText()
                val events = try {
                    listOf(Json.decodeFromString<EventAPI>(body))
                } catch (e: SerializationException) {
                    Json.decodeFromString<List<EventAPI>>(body)
                } catch (e: SerializationException) {
                    return@authorization call.respond(HttpStatusCode.BadRequest)
                }
                val eventsWithUserData = events.map { event ->
                    event.toEvent().let {
                        it.copy(source = it.source.copy(user = user.name, client = user.client))
                    }
                }
                eventProcessor.addEvents(eventsWithUserData)
                repository.insertEvents(eventsWithUserData)
                call.respond(HttpStatusCode.OK)
            }
        }
        webSocket("/sync") {
            authorization { user ->
                suspend fun sendMissedPlays() {
                    val plays = repository.getMissedPlays(user.client)
                    val playsToSend = plays.filter { it.client != user.client }
                    if (playsToSend.isNotEmpty())
                        sendSerialized(playsToSend.map {
                            PlayAPI.fromPlay(it)
                        })
                    // TODO: acknowledgement system
                    if (plays.isNotEmpty())
                        repository.setLastSeenId(user.client, plays.last().id!!)
                }

                sendMissedPlays()
                onUpdate.collectLatest {
                    sendMissedPlays()
                }
            }
        }
    }
}
package com.reas.tracker2.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun httpClient() = HttpClient(CIO) {
    install(HttpCache)
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
    install(Logging) {
        logger = object: Logger {
            override fun log(message: String) {
                co.touchlab.kermit.Logger.v("KtorClient") { message }
            }
        }
    }
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
}
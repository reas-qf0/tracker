package com.reas.tracker2.database

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.koin.java.KoinJavaComponent.getKoin

data class User(
    val name: String,
    val device: String
)

suspend fun RoutingContext.optionalAuthorization(
    inner: suspend RoutingContext.(User?) -> Unit
) {
    val key = call.request.queryParameters["api_key"] ?: return inner(null)

    val repository by getKoin().inject<Repository>()
    inner(repository.getUser(key)?.let {
        User(it, key)
    })
}

suspend fun RoutingContext.authorization(
    inner: suspend RoutingContext.(User) -> Unit
) {
    optionalAuthorization { user ->
        if (user == null)
            return@optionalAuthorization call.respond(HttpStatusCode.Unauthorized)
        inner(user)
    }
}

suspend fun DefaultWebSocketServerSession.optionalAuthorization(
    inner: suspend DefaultWebSocketServerSession.(User?) -> Unit
) {
    val key = call.request.queryParameters["api_key"] ?: return inner(null)

    val repository by getKoin().inject<Repository>()
    inner(repository.getUser(key)?.let {
        User(it, key)
    })
}

suspend fun DefaultWebSocketServerSession.authorization(
    inner: suspend DefaultWebSocketServerSession.(User) -> Unit
) {
    optionalAuthorization { user ->
        if (user == null)
            return@optionalAuthorization call.respond(HttpStatusCode.Unauthorized)
        inner(user)
    }
}
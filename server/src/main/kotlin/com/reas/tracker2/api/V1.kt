package com.reas.tracker2.api

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.reas.tracker2.UserPrincipal
import com.reas.tracker2.database.AuthRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.util.*
import kotlin.time.Instant

@Serializable
private data class V1LoginInfo(
    val username: String,
    val password: String
)

@Serializable
private data class V1TokenInfo(
    val prefix: String,
    val hash: String,
    val createdAt: Instant,
    val expiresAt: Instant,
)

fun Route.v1() {
    val authRepository: AuthRepository by inject()
    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()

    post("/register") {
        val loginInfo = call.receive<V1LoginInfo>()
        try {
            authRepository.addUser(loginInfo.username, loginInfo.password)
        } catch (e: AuthRepository.UserAlreadyExistsException) {
            return@post call.respond(HttpStatusCode.Conflict, e.message)
        }
        call.respond(HttpStatusCode.OK)
    }
    post("/login") {
        val loginInfo = call.receive<V1LoginInfo>()
        if (!authRepository.validate(loginInfo.username, loginInfo.password))
            return@post call.respond(HttpStatusCode.Unauthorized, "username or password are incorrect")

        val token = JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("username", loginInfo.username)
            .withExpiresAt(Date(System.currentTimeMillis() + 36000000))
            .sign(Algorithm.HMAC256(secret))
        call.respond(token)
    }

    authenticate("auth-jwt") {
        route("/clients") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                call.respond(authRepository.getClients(username))
            }
            post("/{name}") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                val clientName = call.requirePathParameter("name")
                authRepository.addClient(username, clientName)
                call.respond(HttpStatusCode.OK)
            }
            delete("/{name}") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                val clientName = call.requirePathParameter("name")
                authRepository.deleteClient(username, clientName)
                call.respond(HttpStatusCode.OK)
            }
        }
        route("/tokens/{clientName}") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                val clientName = call.requirePathParameter("clientName")
                val tokens = authRepository.getTokens(username, clientName)
                call.respond(tokens.map {
                    V1TokenInfo(it.prefix, it.hash.toHexString(), it.createdAt, it.expiresAt)
                })
            }
            post {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                val clientName = call.requirePathParameter("clientName")
                val expiresAt = call.request.queryParameters["expiresAt"]
                    ?.toLongOrNull()
                    ?.let { Instant.fromEpochSeconds(it, 0) }
                    ?: Instant.DISTANT_FUTURE
                val token = authRepository.addToken(username, clientName, expiresAt)
                call.respond(token)
            }
            delete {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                val clientName = call.requirePathParameter("clientName")
                authRepository.deleteAllTokens(username, clientName)
                call.respond(HttpStatusCode.OK)
            }
        }
        delete("/token") {
            val principal = call.principal<JWTPrincipal>()
            val username = principal!!.payload.getClaim("username").asString()
            val token = call.receiveText()
            authRepository.deleteToken(username, token)
            call.respond(HttpStatusCode.OK)
        }
        delete("/tokenHash") {
            val principal = call.principal<JWTPrincipal>()
            val username = principal!!.payload.getClaim("username").asString()
            val token = call.receiveText().hexToByteArray()
            authRepository.deleteTokenByHash(username, token)
            call.respond(HttpStatusCode.OK)
        }
    }

    authenticate("api-token") {
        get("/test") {
            val principal = call.principal<UserPrincipal>()!!
            call.respond("Authenticated as user = ${principal.username} client = ${principal.clientName}")
        }
    }
}
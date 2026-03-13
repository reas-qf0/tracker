package com.reas.tracker2.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kermit.Logger
import com.reas.tracker2.network.TrackerInstanceClient
import com.reas.tracker2.settings.*
import com.reas.tracker2.ui.navigation.ApplicationState
import com.reas.tracker2.ui.navigation.Error
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.seconds

@Composable
fun TrackerBackgroundProcesses(applicationState: ApplicationState) {
    val instanceClient: TrackerInstanceClient = koinInject()
    val settings: Settings = koinInject()

    // server connect loop
    val hostName by settings.flow(instanceHostName).collectAsStateWithLifecycle("")
    val port by settings.flow(instancePort).collectAsStateWithLifecycle(0)
    val username by settings.flow(username).collectAsStateWithLifecycle("")
    LaunchedEffect(hostName, port, username) {
        if (hostName.isEmpty()) return@LaunchedEffect
        if (port == 0) return@LaunchedEffect
        if (username.isEmpty()) return@LaunchedEffect

        val response = instanceClient.tryLogin(hostName, port, username)
        Logger.d { response.toString() }
        if (response != null) {
            applicationState.navigate(Error("Failed to login: \n$response"))
            return@LaunchedEffect
        }

        while (true) {
            if (!instanceClient.tryEstablishConnection())
                delay(15.seconds)
        }
    }
}
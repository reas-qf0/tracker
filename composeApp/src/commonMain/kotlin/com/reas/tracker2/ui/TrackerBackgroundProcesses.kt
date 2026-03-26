package com.reas.tracker2.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reas.tracker2.database.Repository
import com.reas.tracker2.network.TrackerInstanceClient
import com.reas.tracker2.settings.*
import com.reas.tracker2.shared.EventProcessor
import com.reas.tracker2.shared.HolePlugger
import com.reas.tracker2.ui.navigation.ApplicationState
import com.reas.tracker2.ui.navigation.Error
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.seconds

@Composable
fun TrackerBackgroundProcesses(applicationState: ApplicationState) {
    val instanceClient: TrackerInstanceClient = koinInject()
    val settings: Settings = koinInject()
    val repository: Repository = koinInject()
    val eventProcessor: EventProcessor = koinInject()
    val holePlugger: HolePlugger = koinInject()

    // event processing
    LaunchedEffect(Unit) {
        eventProcessor.processQueue()
    }
    LaunchedEffect(Unit) {
        repository.getEventsInQueue().collect { events ->
            eventProcessor.addEvents(events)
        }
    }
    LaunchedEffect(Unit) {
        eventProcessor.collectPlays { plays ->
            repository.insertPlays(plays)
        }
    }

    // plugging holes
    LaunchedEffect(Unit) {
        repository.getNowPlayingTracks().collect { plays ->
            holePlugger.cancelAll()
            plays.forEach { play ->
                holePlugger.register(play)
            }
        }
    }
    LaunchedEffect(Unit) {
        holePlugger.collectPlays { play ->
            repository.insertPlay(play)
        }
    }

    // server connect loop
    val hostName by settings.flow(instanceHostName).collectAsStateWithLifecycle("")
    val port by settings.flow(instancePort).collectAsStateWithLifecycle(0)
    val username by settings.flow(username).collectAsStateWithLifecycle("")
    LaunchedEffect(hostName, port, username) {
        if (hostName.isEmpty()) return@LaunchedEffect
        if (port == 0) return@LaunchedEffect
        if (username.isEmpty()) return@LaunchedEffect

        val response = instanceClient.tryLogin()
        if (response != null) {
            applicationState.navigate(Error("Failed to login: \n$response"))
            return@LaunchedEffect
        }

        while (true) {
            // returns false if the connection wasn't established, true if the connection was established and closed
            // reconnect immediately if the latter
            if (!instanceClient.tryEstablishConnection())
                delay(15.seconds)
        }
    }
}
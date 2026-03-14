package com.reas.tracker2.ui

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kermit.Logger
import com.reas.tracker2.database.Repository
import com.reas.tracker2.network.TrackerInstanceClient
import com.reas.tracker2.settings.*
import com.reas.tracker2.shared.EventProcessor
import com.reas.tracker2.ui.navigation.ApplicationState
import com.reas.tracker2.ui.navigation.Error
import kotlinx.coroutines.*
import org.koin.compose.koinInject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

private const val TAG = "TrackerBackgroundProcesses"

@Composable
fun TrackerBackgroundProcesses(applicationState: ApplicationState) {
    val instanceClient: TrackerInstanceClient = koinInject()
    val settings: Settings = koinInject()
    val repository: Repository = koinInject()
    val eventProcessor: EventProcessor = koinInject()
    val scope: CoroutineScope = rememberCoroutineScope()

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
        eventProcessor.collectPlays { play ->
            repository.insertPlay(play)
        }
    }

    // plugging holes
    val plugJobs = remember { mutableMapOf<String, Job>() }
    LaunchedEffect(Unit) {
        repository.getNowPlayingTracks().collect { plays ->
            val playsMap = plays.associateBy { it.key }
            val oldKeys = plugJobs.keys.toSet()
            val newKeys = playsMap.keys.toSet()
            Logger.d(tag = TAG) { "$oldKeys $newKeys" }

            oldKeys.forEach { key ->
                Logger.d(tag = TAG) { "cancelling job to plug hole for $key" }
                plugJobs[key]!!.cancel()
                plugJobs.remove(key)
            }
            newKeys.forEach { key ->
                val play = playsMap[key]!!
                val delayTime = play.duration - play.lastPosition - (Clock.System.now() - play.lastTimestamp)
                Logger.d(tag = TAG) { "launching job to plug hole for $key in $delayTime" }
                plugJobs[key] = scope.launch(Dispatchers.IO) {
                    delay(delayTime)
                    Logger.d(tag = TAG) { "plugging hole for $key" }
                    plugJobs.remove(key) // so that no one can cancel us
                    play.timePlayed += play.duration - play.lastPosition
                    play.associatedEvents.add(null)
                    repository.insertPlay(play)
                }
            }
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

        val response = instanceClient.tryLogin(hostName, port, username)
        Logger.d { response.toString() }
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
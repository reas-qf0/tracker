package com.reas.tracker2.shared

import co.touchlab.kermit.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.time.Clock

class HolePlugger {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val plugJobs = mutableMapOf<String, Job>()
    private val playFlow = MutableSharedFlow<Play>()

    fun register(play: Play) {
        if (!play.lastPlaying)
            return
        val key = play.key
        if (plugJobs.containsKey(key))
            cancel(play)
        val delayTime = play.duration - play.lastPosition - (Clock.System.now() - play.lastTimestamp)
        Logger.d(tag = TAG) { "launching job to plug hole for $key in $delayTime" }
        plugJobs[key] = scope.launch {
            delay(delayTime)
            Logger.d(tag = TAG) { "plugging hole for $key" }
            plugJobs.remove(key) // so that no one can cancel us
            play.timePlayed += play.duration - play.lastPosition
            play.associatedEvents.add(null)
            playFlow.emit(play)
        }
    }

    fun register(plays: List<Play>) {
        plays.forEach { play ->
            register(play)
        }
    }

    fun cancel(play: Play) {
        val key = play.key
        if (plugJobs.containsKey(key)) {
            Logger.d(tag = TAG) { "cancelling job to plug hole for $key" }
            plugJobs[key]!!.cancel()
            plugJobs.remove(key)
        }
    }

    fun cancelAll() {
        plugJobs.values.forEach { it.cancel() }
    }

    suspend fun collectPlays(block: suspend (Play) -> Unit) {
        playFlow.collect(block)
    }

    companion object {
        private const val TAG = "HolePlugger"
    }
}
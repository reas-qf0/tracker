package com.reas.tracker2.shared

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.time.Duration.Companion.seconds

class EventProcessor(
    private val adapter: EventProcessorAdapter
) {
    companion object {
        val SKIP_MIN_DURATION = 2.seconds
        private const val TAG = "EventProcessor"
    }

    private val eventFlow = MutableSharedFlow<List<Event>>()
    private val playFlow = MutableSharedFlow<List<Play>>()

    suspend fun addEvents(events: List<Event>) {
        eventFlow.emit(events)
    }
    suspend fun collectPlays(block: suspend (List<Play>) -> Unit) {
        playFlow.collect { block(it) }
    }

    private suspend fun process(events: List<Event>) {
        if (events.isEmpty()) return

        val source = events[0].source
        var play = adapter.getLastPlayFromSource(source)
        val eventsSorted = events.sortedBy { it.timestamp }
        if (play != null && eventsSorted[0].timestamp < play.timestamp) {
            Logger.e(tag = TAG) {
                "ERROR: out-of-sync events source=$source " +
                "eventTimestamp=${eventsSorted[0].timestamp} playTimestamp=${play.timestamp}"
            }
            return
        }

        val resultPlays = mutableListOf<Play>()
        suspend fun flush(event: Event, play: Play?): Play {
            if (play != null) {
                if (play.lastPlaying)
                    play.associatedEvents.add(null)
                resultPlays.add(play)
            }
            return Play.fromEvent(event, id = adapter.getNextId(source.user))
        }

        eventsSorted.forEach { event ->
            if (play == null) {
                if (event.isPlaying) play = flush(event, play)
                return@forEach
            }

            // check if need to plug hole
            val eventInfo = Play.EventInfo.fromEvent(event)
            val shouldPlugHole = event.timestamp > play.endTimestamp
            if (play.lastPlaying && shouldPlugHole) {
                play.timePlayed += play.duration - play.lastPosition
                play.associatedEvents.add(null)
            }
            if (play.associatedEvents.last() == null && !shouldPlugHole) {
                play.timePlayed -= play.duration - play.lastPosition
                play.associatedEvents.removeAt(play.associatedEvents.size - 1)
            }
            if (play.lastPlaying && !shouldPlugHole) {
                play.timePlayed += event.timestamp - play.lastTimestamp
            }

            if (event.isPlaying) {
                if (event.position <= SKIP_MIN_DURATION) {
                    if (event.metadata == play.metadata && play.lastPosition <= SKIP_MIN_DURATION) {
                        play.associatedEvents.add(eventInfo)
                    } else {
                        play = flush(event, play)
                    }
                } else {
                    if (event.metadata == play.metadata) {
                        play.associatedEvents.add(eventInfo)
                    } else {
                        play = flush(event, play)
                    }
                }
            } else {
                play.associatedEvents.add(eventInfo)
            }
        }

        if (play != null)
            resultPlays.add(play)
        adapter.clearQueue(source, eventsSorted.last().timestamp)
        playFlow.emit(resultPlays)
    }

    suspend fun processQueue() {
        eventFlow.collect { snapshot ->
            Logger.d(tag = TAG) { "processing ${snapshot.size} events" }
            snapshot.groupBy { it.source }.forEach {
                process(it.value)
            }
        }
    }
}
package com.reas.tracker2.shared

import co.touchlab.kermit.Logger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class EventProcessor(
    private val adapter: EventProcessorAdapter
) {
    companion object {
        val SKIP_MIN_DURATION = 2.seconds
        private const val TAG = "EventProcessor"
    }

    private suspend fun flush(event: Event, play: Play?): Play {
        if (play != null) {
            if (play.lastPlaying)
                play.associatedEvents.add(null)
            adapter.insertPlay(play)
        }
        return Play(
            metadata = event.metadata,
            timestamp = event.timestamp,
            timePlayed = Duration.ZERO,
            source = event.source,
            associatedEvents = mutableListOf(Play.EventInfo.fromEvent(event))
        )
    }

    private suspend fun process(events: List<Event>): Play? {
        if (events.isEmpty()) return null

        val source = events[0].source
        var play = adapter.getLastPlayFromSource(source)
        val eventsSorted = events.sortedBy { it.timestamp }
        if (play != null && eventsSorted[0].timestamp < play.timestamp) {
            Logger.e(TAG) {
                "ERROR: out-of-sync events source=$source " +
                "eventTimestamp=${eventsSorted[0].timestamp} playTimestamp=${play!!.timestamp}"
            }
            return null
        }

        eventsSorted.forEach { event ->
            // check if need to plug hole
            if (play == null) {
                if (event.isPlaying) play = flush(event, play)
                return@forEach
            }

            val eventInfo = Play.EventInfo.fromEvent(event)
            val shouldPlugHole = event.timestamp - play.lastTimestamp + play.lastPosition > play.duration
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
            adapter.insertPlay(play)
        adapter.clearQueue(source, eventsSorted.last().timestamp)

        if (play?.lastPlaying ?: false)
            return play
        return null
    }

    suspend fun processQueue() {
        adapter.getEvents().collect { snapshot ->
            Logger.d(TAG) { "processing ${snapshot.size} events" }
            snapshot.groupBy { it.source }.forEach {
                process(it.value)
            }
        }
    }
}
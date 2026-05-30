package com.reas.tracker2.shared

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.time.Duration.Companion.seconds

class EventProcessor(
    private val adapter: EventProcessorAdapter
) {
    companion object {
        val SKIP_MIN_DURATION = 2.seconds
        private val logger = KotlinLogging.logger {}
    }

    private val eventFlow = MutableSharedFlow<List<Event>>(replay = 1)
    private val playFlow = MutableSharedFlow<List<Play>>(replay = 1)

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
            logger.error {
                "ERROR: out-of-sync events source=$source " +
                "eventTimestamp=${eventsSorted[0].timestamp} playTimestamp=${play!!.timestamp}"
            }
            return
        }

        val resultPlays = mutableListOf<Play>()
        suspend fun flush(event: Event, play: Play?): Play {
            if (play != null) {
                if (play.lastPlaying) {
                    play.associatedEvents.add(EventInfo(
                        timestamp = event.timestamp,
                        position = play.lastPosition + (event.timestamp - play.lastTimestamp),
                        state = EventState.PLUGGED
                    ))
                }
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
            val shouldPlugHole = event.timestamp > play.endTimestamp
            if (play.lastPlaying && shouldPlugHole) {
                play.timePlayed += play.duration - play.lastPosition
                play.associatedEvents.add(EventInfo(
                    position = play.duration,
                    timestamp = play.endTimestamp,
                    state = EventState.PLUGGED
                ))
            }
            if (play.associatedEvents.last().state == EventState.PLUGGED && !shouldPlugHole) {
                play.associatedEvents.removeAt(play.associatedEvents.size - 1)
                play.timePlayed -= play.duration - play.lastPosition
            }
            if (play.lastPlaying && !shouldPlugHole) {
                play.timePlayed += event.timestamp - play.lastTimestamp
            }

            if (event.isPlaying) {
                if (event.position <= SKIP_MIN_DURATION) {
                    if (event.metadata == play.metadata && play.lastPosition <= SKIP_MIN_DURATION) {
                        play.associatedEvents.add(event.info)
                    } else {
                        play = flush(event, play)
                    }
                } else {
                    if (event.metadata == play.metadata) {
                        play.associatedEvents.add(event.info)
                    } else {
                        play = flush(event, play)
                    }
                }
            } else {
                if (event.position <= SKIP_MIN_DURATION && play.lastPosition > SKIP_MIN_DURATION) {
                    play = flush(event, play)
                } else {
                    play.associatedEvents.add(event.info)
                }
            }
        }

        if (play != null)
            resultPlays.add(play)
        adapter.clearQueue(source, eventsSorted.last().timestamp)
        playFlow.emit(resultPlays)
    }

    suspend fun processQueue() {
        eventFlow.collect { snapshot ->
            logger.debug { "processing ${snapshot.size} events" }
            snapshot.groupBy { it.source }.forEach {
                process(it.value)
            }
        }
    }
}
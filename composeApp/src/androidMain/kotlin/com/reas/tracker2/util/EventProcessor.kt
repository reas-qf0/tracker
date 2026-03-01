package com.reas.tracker2.util

import android.util.Log
import com.reas.tracker2.database.objects.Event
import com.reas.tracker2.database.Repository
import com.reas.tracker2.database.objects.Play

class EventProcessor(
    private val repository: Repository
) {
    companion object {
        const val SKIP_MIN_DURATION = 2000L
        private const val TAG = "EventProcessor"
    }

    private suspend fun flush(event: Event, play: Play?): Play {
        if (play != null)
            repository.insertPlay(play)
        return Play(
            track = event.track,
            artist = event.artist,
            album = event.album,
            albumArtist = event.albumArtist,
            timestamp = event.timestamp,
            duration = event.duration,
            timePlayed = 0L,
            lastPosition = event.position,
            lastPlaying = event.isPlaying,
            sourceDevice = "",
            sourceApp = event.sourceApp,
            associatedEvents = mutableListOf(event.timestamp)
        )
    }

    private suspend fun process(events: List<Event>): Play? {
        // it is assumed that all events from the list have the same sourceApp
        if (events.isEmpty()) return null

        val app = events[0].sourceApp
        var play = repository.getLastPlayFromSource("", app)
        val eventsSorted = events.sortedBy { it.timestamp }
        if (play != null && eventsSorted[0].timestamp < play.timestamp) {
            Log.e(TAG, "ERROR: out-of-sync events app=$app eventTimestamp=${eventsSorted[0].timestamp} playTimestamp=${play.timestamp}")
            return null
        }

        eventsSorted.forEach { event ->
            // check if need to plug hole
            if (play == null) {
                play = flush(event, play)
                return@forEach
            }

            val shouldPlugHole = event.timestamp - play.lastTimestamp + play.lastPosition > play.duration
            if (play.lastPlaying && shouldPlugHole) {
                play.lastPlaying = false
                play.timePlayed += play.duration - play.lastPosition
                play.associatedEvents.add(0L)
            }
            if (play.associatedEvents.last() == 0L && !shouldPlugHole) {
                play.lastPlaying = true
                play.timePlayed -= play.duration - play.lastPosition
                play.associatedEvents.removeAt(play.associatedEvents.size - 1)
            }
            if (play.lastPlaying && !shouldPlugHole) {
                play.timePlayed += event.timestamp - play.lastTimestamp
            }

            if (event.isPlaying) {
                if (event.position <= SKIP_MIN_DURATION) {
                    if (event.metadataEqual(play) && play.lastPosition <= SKIP_MIN_DURATION) {
                        play.lastPlaying = event.isPlaying
                        play.lastPosition = event.position
                        play.associatedEvents.add(event.timestamp)
                    } else {
                        play.lastPlaying = false
                        play = flush(event, play)
                    }
                } else {
                    if (event.metadataEqual(play)) {
                        play.lastPlaying = event.isPlaying
                        play.lastPosition = event.position
                        play.associatedEvents.add(event.timestamp)
                    } else {
                        play.lastPlaying = false
                        play = flush(event, play)
                    }
                }
            } else {
                play.lastPlaying = event.isPlaying
                play.lastPosition = event.position
                play.associatedEvents.add(event.timestamp)
            }
        }

        if (play != null)
            repository.insertPlay(play)
        repository.clearQueue(app, eventsSorted.last().timestamp)

        if (play!!.lastPlaying)
            return play
        return null
    }

    suspend fun processQueue() {
        repository.getEvents().collect { snapshot ->
            Log.d(TAG, "processing ${snapshot.size} events")
            snapshot.groupBy { it.sourceApp }.forEach {
                process(it.value)
            }
        }
    }
}
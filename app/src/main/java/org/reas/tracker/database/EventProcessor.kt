package org.reas.tracker.database

import org.reas.tracker.AppDataContainer

class EventProcessor(private val container: AppDataContainer) {
    companion object {
        const val SKIP_MIN_DURATION = 2000L
    }

    suspend fun feed(event: Event, sync: Boolean = false) {
        val lastEvent = container.repository.getLastEventFromPlayer(event.playerId)
        if (lastEvent == null) {
            // first ever message from this player
            container.repository.insertEvent(event)
            container.repository.insertPlay(Play.fromEvent(event))
            return
        }
        if (lastEvent.isEqual(event)) {
            // duplicate message from MediaListener
            return
        }
        if (lastEvent.timestamp > event.timestamp) {
            // EventProcessor only works well with monotonous timestamps
            // grab all events from this player and rescans them
            // a hack but works for now
            val events = container.repository.getEventsFromPlayer(event.playerId)
            feedBatch(events, true)
        }

        // event is valid - save & show
        container.repository.insertEvent(event)
        if (!sync)
            container.cloudSave.submitEvent(event)

        val lastPlay = container.repository.getLastPlayFromPlayer(event.playerId)
        if (lastPlay == null) {
            throw RuntimeException("lastEvent without an associated Play?")
        }
        if (lastEvent.isPlaying)
            lastPlay.timePlayed += event.timestamp - lastEvent.timestamp

        if (event.isPlaying && (!event.metadataEqual(lastPlay) || event.position < SKIP_MIN_DURATION)) {
            // start event for a new track / restart of the same track
            // first we need to send a stop event for the last track if there isn't one
            if (lastEvent.isPlaying) {
                val newEvent = lastEvent.copy(timestamp = event.timestamp, isPlaying = false)
                feed(newEvent)
            }
            // & replace the cached play for this app
            val play = Play.fromEvent(event)
            container.repository.insertPlay(play)
        } else {
            if (!event.isPlaying && !lastEvent.isPlaying)
                return // duplicate stop event
            lastPlay.associatedEvents.add(event.id)
            lastPlay.updateState(container.repository.getEvents(lastPlay.associatedEvents))
            container.repository.updatePlay(lastPlay)
        }
    }

    suspend fun feedBatch(events: List<Event>, sync: Boolean = false) {
        events.sortedWith { event1, event2 ->            // sort each group by (timestamp, isPlaying)
            if (event1.timestamp != event2.timestamp)
                event1.timestamp.compareTo(event2.timestamp)
            else event1.isPlaying.compareTo(event2.isPlaying)
        }.forEach { event ->
            feed(event, sync)
        }
    }
}
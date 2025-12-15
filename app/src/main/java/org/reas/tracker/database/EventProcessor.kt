package org.reas.tracker.database

object EventProcessor {
    const val SKIP_MIN_DURATION = 2000L
    private val unfinishedPlays = mutableMapOf<String, Play>()
    private val cachedEvents = mutableMapOf<Long, Event>()

    private fun playFromEvent(event: Event): Play = Play(
        event.track,
        event.artist,
        event.album,
        event.albumArtist,
        event.timestamp,
        event.duration,
        0L,
        mutableListOf(event.id)
    )

    suspend fun feed(repository: Repository, event: Event) {
        event.id = repository.insertEvent(event)

        cachedEvents[event.id] = event
        val app = event.app
        val current = unfinishedPlays[app]

        if (current == null) {
            // first event from this app
            if (event.isPlaying) {
                val play = playFromEvent(event)
                unfinishedPlays[app] = play
                play.id = repository.insertPlay(play)
            }
            return
        }

        val lastEvent = cachedEvents[current.associatedEvents.last()]!!

        if (event.isPlaying && (!event.metadataEqual(current) || event.position < SKIP_MIN_DURATION)) {
            // start event for a new track / restart of the same track
            // first we need to send a stop event for the last track if there isn't one
            if (lastEvent.isPlaying) {
                val newEvent = lastEvent.copy(timestamp = event.timestamp, isPlaying = false)
                feed(repository, newEvent)
            }
            // & replace the cached play for this app
            for (eventId in current.associatedEvents)
                cachedEvents.remove(eventId)
            val play = playFromEvent(event)
            unfinishedPlays[app] = play
            play.id = repository.insertPlay(play)
        } else {
            // no need to do anything, just tally up the time
            if (lastEvent.isPlaying)
                current.timePlayed += event.timestamp - lastEvent.timestamp
            current.associatedEvents.add(event.id)
            repository.updatePlay(current)
        }
    }
}
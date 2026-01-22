package org.reas.tracker.database

import android.app.Notification
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.flow.first
import org.reas.tracker.AppDataContainer
import org.reas.tracker.MainActivity
import org.reas.tracker.R
import org.reas.tracker.android.DataStoreWrapper.Companion.SCROBBLING_ENABLED
import org.reas.tracker.android.NotificationWrapper
import kotlin.jvm.java
import kotlin.math.min
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class EventProcessor(private val container: AppDataContainer) {
    companion object {
        const val SKIP_MIN_DURATION = 2000L
        private const val TAG = "EventProcessor"
    }

    private val repository = container.repository
    private val preferences = container.preferences
    private var notificationId = NotificationWrapper.reserveId()
    private var notificationBuilder: (Notification.Builder.() -> Unit)? = null

    private fun updateNotification(event: Event) {
        Log.d(TAG, "updateNotification")
        if (event.isPlaying) {
            notificationBuilder = {
                setContentTitle(event.track)
                setContentText(event.artist)
                setSmallIcon(R.drawable.ic_launcher_foreground)

                val resultIntent = Intent(container.context, MainActivity::class.java)
                val resultPendingIntent: PendingIntent? =
                    TaskStackBuilder.create(container.context).run {
                        addNextIntentWithParentStack(resultIntent)
                        getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    }
                setContentIntent(resultPendingIntent)
            }
            NotificationWrapper.show(
                container.context,
                "Now Playing",
                notificationId,
                notificationBuilder!!
            )
        } else {
            notificationBuilder = null
            NotificationWrapper.hide(notificationId)
        }
    }

    private suspend fun plugHole(play: Play): Boolean {
        if (play.lastPlaying && play.endTimestamp + SKIP_MIN_DURATION < System.currentTimeMillis()) {
            Log.d(TAG, "plugHole $play")
            val event = Event(
                track = play.track,
                artist = play.artist,
                album = play.album,
                albumArtist = play.artist,
                playerId = play.playerId,
                timestamp = play.endTimestamp,
                position = play.duration,
                duration = play.duration,
                isPlaying = false
            )
            play.lastPlaying = false
            play.timePlayed += play.endTimestamp - play.lastTimestamp
            play.associatedEvents.add(event.id)

            repository.insertEvent(event)
            repository.updatePlay(play)
            return true
        }
        return false
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun feed(event: Event, sync: Boolean = false) {
        if (!sync && !preferences.getValue(SCROBBLING_ENABLED, true)) {
            // scrobbling is disabled
            return
        }
        if (container.repository.getEvents(listOf(event.id)).isNotEmpty()) {
            // event already in local database => processed, do nothing
            return
        }

        repository.getNowPlayingTracks().first().forEach { play ->
            plugHole(play)
        }

        Log.d(TAG, "feed $event")
        val lastEvent = container.repository.getLastEventFromPlayer(event.playerId)
        if (lastEvent == null) {
            container.repository.insertPlay(Play.fromEvent(event))
        } else {
            if (lastEvent.isEqual(event)) {
                // duplicate message from MediaListener
                return
            }

            if (lastEvent.timestamp > event.timestamp) {
                // EventProcessor only works well with monotonous timestamps
                // grab all events from this player and rescans them
                // a hack but works for now
                val player = event.playerId
                Log.d(TAG, "out-of-sync events; reprocessing all events from $player")
                val events = container.repository.getEventsFromPlayer(player)
                repository.deleteEventsFromPlayer(player)
                repository.clearPlaysFromPlayer(player)
                if (sync) {
                    Log.e(TAG, "!!! resync shouldn't happen twice, aborting")
                } else {
                    feedBatch(events, true)
                }
            }

            val lastPlay = repository.getLastPlayFromPlayer(event.playerId)
            if (lastPlay == null) {
                throw RuntimeException("lastEvent without an associated Play?")
            }

            if (lastEvent.isPlaying)
                lastPlay.timePlayed += event.timestamp - lastEvent.timestamp

            if (event.isPlaying && (!event.metadataEqual(lastPlay) || event.position < SKIP_MIN_DURATION)) {
                // start event for a new track / restart of the same track
                // first we need to send a stop event for the last track if there isn't one
                if (lastEvent.isPlaying) {
                    val newEvent = lastEvent.copy(
                        id = Uuid.random().toHexDashString(),
                        position = min(lastEvent.duration, lastEvent.position + event.timestamp - lastEvent.timestamp) - 1,
                        timestamp = event.timestamp - 1,
                        isPlaying = false
                    )
                    feed(newEvent)
                }
                // & replace the cached play for this app
                val play = Play.fromEvent(event)
                container.repository.insertPlay(play)
            } else {
                if (!event.isPlaying && !lastEvent.isPlaying)
                    return // duplicate stop event
                lastPlay.associatedEvents.add(event.id)
                lastPlay.lastPosition = event.position
                lastPlay.lastTimestamp = event.timestamp
                lastPlay.lastPlaying = event.isPlaying
                repository.updatePlay(lastPlay)
            }
        }

        // event is valid - save & show
        repository.insertEvent(event)
        if (!sync)
            container.cloudSave.submitEvent(event)
        updateNotification(event)
    }

    suspend fun feedBatch(events: List<Event>, sync: Boolean = false) {
        Log.d(TAG, "feedBatch: ")
        val sortedEvents = events.sortedWith { event1, event2 ->            // sort each group by (timestamp, isPlaying)
            if (event1.timestamp != event2.timestamp)
                event1.timestamp.compareTo(event2.timestamp)
            else event1.isPlaying.compareTo(event2.isPlaying)
        }
        sortedEvents.forEach { event ->
            Log.d(TAG, "    $event")
        }
        sortedEvents.forEach { event ->
            feed(event, sync)
        }
    }
}
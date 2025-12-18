package org.reas.tracker.database

import android.app.Notification
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.delay
import org.reas.tracker.AppDataContainer
import org.reas.tracker.MainActivity
import org.reas.tracker.R
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
    private val notificationId = NotificationWrapper.reserveId()
    private var notificationBuilder: (Notification.Builder.() -> Unit)? = null

    private fun updateNotification(event: Event) {
        Log.d(TAG, "updateNotification")
        notificationBuilder = {
            setContentTitle(event.track)
            setContentText(event.artist)
            setSmallIcon(R.drawable.ic_launcher_foreground)

            val resultIntent = Intent(container.context, MainActivity::class.java)
            val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(container.context).run {
                addNextIntentWithParentStack(resultIntent)
                getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }
            setContentIntent(resultPendingIntent)
        }
    }

    suspend fun displayNotificationLoop() {
        while (true) {
            val b = notificationBuilder
            if (b != null)
                NotificationWrapper.show(container.context, "Now Playing", notificationId, b)
            else
                NotificationWrapper.hide(notificationId)
            delay(1000)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun feed(event: Event, sync: Boolean = false) {
        if (container.repository.getEvents(listOf(event.id)).isNotEmpty()) {
            // event already in local database => processed, do nothing
            return
        }

        Log.d(TAG, "feed $event")
        var lastEvent = container.repository.getLastEventFromPlayer(event.playerId)
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
                feedBatch(events, true)
            }

            val lastPlay = repository.getLastPlayFromPlayer(event.playerId)
            if (lastPlay == null) {
                throw RuntimeException("lastEvent without an associated Play?")
            }

            // plug the hole caused by e.g. notif service getting killed
            if (lastPlay.state == Play.PLAYING &&
                lastPlay.endTimestamp + SKIP_MIN_DURATION < lastEvent.timestamp &&
                lastPlay.endTimestamp + SKIP_MIN_DURATION < System.currentTimeMillis()) {
                val event = Event(
                    track = lastPlay.track,
                    artist = lastPlay.artist,
                    album = lastPlay.album,
                    albumArtist = lastPlay.artist,
                    playerId = lastPlay.playerId,
                    timestamp = lastPlay.endTimestamp,
                    position = lastPlay.duration,
                    duration = lastPlay.duration,
                    isPlaying = false
                )
                feed(event)
                lastEvent = event
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
                lastPlay.updateState(event.isPlaying)
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
        Log.d(TAG, "feedBatch $events")
        events.sortedWith { event1, event2 ->            // sort each group by (timestamp, isPlaying)
            if (event1.timestamp != event2.timestamp)
                event1.timestamp.compareTo(event2.timestamp)
            else event1.isPlaying.compareTo(event2.isPlaying)
        }.forEach { event ->
            feed(event, sync)
        }
    }
}
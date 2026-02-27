package com.reas.tracker2.android

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.ComponentName
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import android.util.Log
import androidx.core.content.getSystemService
import com.reas.tracker2.MainActivity
import com.reas.tracker2.R
import com.reas.tracker2.database.objects.Event
import com.reas.tracker2.database.Repository
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private val MediaMetadata.title
    get() = this.getString(MediaMetadata.METADATA_KEY_TITLE)
private val MediaMetadata.artist
    get() = this.getString(MediaMetadata.METADATA_KEY_ARTIST)
private val MediaMetadata.album
    get() = this.getString(MediaMetadata.METADATA_KEY_ALBUM)
private val MediaMetadata.albumArtist
    get() = this.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
private val MediaMetadata.duration
    get() = this.getLong(MediaMetadata.METADATA_KEY_DURATION)

private class MediaCallback(
    private val appId: String
): MediaController.Callback(), KoinComponent {
    private val repository: Repository by inject()
    private val notificationManager: NotificationWrapper by inject()

    private var notificationId = notificationManager.reserveId()
    private var notificationBuilder: NotificationBuilder? = null
    private var currentMetadata: MediaMetadata? = null

    private fun updateNotification(event: Event) {
        Log.d(TAG, "updateNotification")
        if (event.isPlaying) {
            notificationBuilder = { context ->
                setContentTitle(event.track)
                setContentText(event.artist)
                setSmallIcon(R.drawable.ic_stat_name)

                val resultIntent = Intent(context, MainActivity::class.java)
                val resultPendingIntent =
                    TaskStackBuilder.create(context).run {
                        addNextIntentWithParentStack(resultIntent)
                        getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    }
                setContentIntent(resultPendingIntent)

                val deleteIntent = Intent(context, MainActivity::class.java)
                deleteIntent.putExtra("org.reas.tracker2.appId", appId)
                val deletePendingIntent = PendingIntent.getService(context, 42, deleteIntent,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
                setDeleteIntent(deletePendingIntent)
            }
        } else {
            notificationBuilder = null
        }
        showNotification()
    }

    private fun showNotification() {
        if (notificationBuilder != null) {
            notificationManager.show(
                "Now Playing",
                notificationId,
                notificationBuilder!!
            )
        } else {
            notificationManager.hide(notificationId)
        }
    }

    override fun onMetadataChanged(metadata: MediaMetadata?) {
        Log.d(TAG, "onMetadataChanged($appId) $metadata")

        if (metadata == null) return
        currentMetadata = metadata
    }

    override fun onPlaybackStateChanged(state: PlaybackState?) {
        Log.d(TAG, "onPlaybackStateChanged($appId) $state")

        if (state == null) return

        currentMetadata?.let {
            val metadata = currentMetadata!!

            val event = Event(
                track = metadata.title,
                artist = metadata.artist,
                album = metadata.album,
                albumArtist = metadata.albumArtist ?: metadata.artist,
                timestamp = state.lastPositionUpdateTime - SystemClock.elapsedRealtime() + System.currentTimeMillis(),
                position = state.position,
                duration = metadata.duration,
                isPlaying = state.state == PlaybackState.STATE_PLAYING,
                sourceApp = appId
            )
            runBlocking {
                repository.insertEvent(event)
            }
            updateNotification(event)
        }
    }

    override fun onSessionDestroyed() {
        Log.d(TAG, "onSessionDestroyed($appId)")
    }

    fun onNotificationDismissed() {
        notificationId = notificationManager.reserveId()
        showNotification()
    }

    companion object {
        private val TAG = "NotificationListenerService"
    }
}

private class SessionListener: MediaSessionManager.OnActiveSessionsChangedListener {
    private var callbacks = mutableMapOf<MediaController, MediaCallback>()

    override fun onActiveSessionsChanged(controllers: List<MediaController>?) {
        Log.d(TAG, "onActiveSessionsChanged $controllers")
        if (controllers == null) return

        val oldControllers = callbacks.keys
        val newControllers = controllers.toMutableSet()
        oldControllers.minus(newControllers).forEach { controller ->
            val callback = callbacks[controller]!!
            controller.unregisterCallback(callback)
            callbacks.remove(controller)
        }
        newControllers.minus(oldControllers).forEach { controller ->
            val callback = MediaCallback(controller.packageName)
            controller.registerCallback(callback)
            controller.metadata?.let { callback.onMetadataChanged(it) }
            controller.playbackState?.let { callback.onPlaybackStateChanged(it) }
            controller.extras?.let { callback.onExtrasChanged(it) }
            callbacks[controller] = callback
        }
    }

    fun onNotificationDismissed(appId: String) {
        Log.d("TAG", "onNotificationDismissed($appId)")
        callbacks.forEach { controller, callback ->
            if (controller.packageName == appId) {
                callback.onNotificationDismissed()
            }
        }
    }

    companion object {
        private const val TAG = "NotificationListenerService"
    }
}

class NotifListenerService: NotificationListenerService() {
    private var initialized = false
    private var listener: SessionListener? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (startId == 42)
            listener!!.onNotificationDismissed(intent!!.getStringExtra("org.reas.tracker2.appId")!!)
        return START_STICKY
    }

    private fun init() {
        if (listener != null) return
        val sessManager = getSystemService<MediaSessionManager>()!!
        val component = ComponentName(this, this::class.java)
        listener = SessionListener()

        sessManager.addOnActiveSessionsChangedListener(listener!!, component)
        listener!!.onActiveSessionsChanged(sessManager.getActiveSessions(component))
    }

    private fun destroy() {
        val sessManager = getSystemService<MediaSessionManager>()!!
        sessManager.removeOnActiveSessionsChangedListener(listener!!)
        listener = null
        initialized = false
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "onListenerConnected")
        if (!initialized) {
            synchronized(this) {
                if (!initialized) {
                    initialized = true
                    init()
                }
            }
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "onListenerDisconnected")
        destroy()
    }

    companion object {
        private const val TAG = "NotificationListenerService"
    }
}
package org.reas.tracker.android

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import android.util.Log
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.reas.tracker.TrackerApplication
import org.reas.tracker.database.Event
import org.reas.tracker.database.EventProcessor

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

private class MediaCallback(private val scope: CoroutineScope, private val appId: String): MediaController.Callback() {
    private val repository = TrackerApplication.instance!!.container.repository
    private var currentMetadata: MediaMetadata? = null
    override fun onMetadataChanged(metadata: MediaMetadata?) {
        Log.d(TAG, "onMetadataChanged($appId) $metadata")

        if (metadata == null) return
        currentMetadata = metadata
    }

    override fun onPlaybackStateChanged(state: PlaybackState?) {
        Log.d(TAG, "onPlaybackStateChanged($appId) $state")

        if (state == null) return

        scope.launch {
            while (currentMetadata == null) {
                // the app just started up, wait for onMetadataChanged
            }
            val metadata = currentMetadata!!

            val event = Event(
                track = metadata.title,
                artist = metadata.artist,
                album = metadata.album,
                albumArtist = metadata.albumArtist,
                app = appId,
                timestamp = state.lastPositionUpdateTime - SystemClock.elapsedRealtime() + System.currentTimeMillis(),
                position = state.position,
                duration = metadata.duration,
                isPlaying = state.state == PlaybackState.STATE_PLAYING
            )
            EventProcessor.feed(repository, event)
        }
    }

    override fun onSessionDestroyed() {
        Log.d(TAG, "onSessionDestroyed($appId)")
    }

    companion object {
        private val TAG = "MediaCallback"
    }
}

private class SessionListener(val scope: CoroutineScope): MediaSessionManager.OnActiveSessionsChangedListener {
    private var controllerMap = hashMapOf<String, MediaController>()

    override fun onActiveSessionsChanged(controllers: List<MediaController>?) {
        if (controllers == null) return

        val newControllerMap = hashMapOf(
            *controllers.map { c -> c.packageName to c }.toTypedArray()
        )

        // register new controllers
        newControllerMap.minus(controllerMap.keys).forEach { pkg, controller ->
            val callback = MediaCallback(scope, controller.packageName)
            controller.registerCallback(callback)
            controller.playbackState?.let { callback.onPlaybackStateChanged(it) }
            controller.metadata?.let { callback.onMetadataChanged(it) }
            controller.extras?.let { callback.onExtrasChanged(it) }
        }

        controllerMap = newControllerMap
    }
}

class NotifListenerService: NotificationListenerService() {
    private var initialized = false
    private val repository = TrackerApplication.instance!!.container.repository
    private lateinit var job: Job
    private lateinit var scope: CoroutineScope
    private lateinit var listener: SessionListener

    private fun init() {
        val sessManager = getSystemService<MediaSessionManager>()!!
        val component = ComponentName(this, this::class.java)
        job = SupervisorJob()
        scope = CoroutineScope(Dispatchers.Main + job)
        listener = SessionListener(scope)

        sessManager.addOnActiveSessionsChangedListener(listener, component)
        listener.onActiveSessionsChanged(sessManager.getActiveSessions(component))
    }

    private fun destroy() {
        val sessManager = getSystemService<MediaSessionManager>()!!
        sessManager.removeOnActiveSessionsChangedListener(listener)
        scope.launch {
            EventProcessor.cleanup(repository)
        }.invokeOnCompletion {
            job.cancel()
        }
    }

    override fun onListenerConnected() {
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
        destroy()
    }
}
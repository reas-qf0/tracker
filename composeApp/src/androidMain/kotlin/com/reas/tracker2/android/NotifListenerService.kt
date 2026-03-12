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
import co.touchlab.kermit.Logger
import com.reas.tracker2.MainActivity
import com.reas.tracker2.R
import com.reas.tracker2.database.Repository
import com.reas.tracker2.network.SyncManager
import com.reas.tracker2.settings.Settings
import com.reas.tracker2.settings.collect
import com.reas.tracker2.settings.get
import com.reas.tracker2.settings.isScrobblingEnabled
import com.reas.tracker2.shared.*
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

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

private const val TAG = "NotificationListenerService"

private class MediaCallback(private val appId: String): MediaController.Callback(), KoinComponent {
    private val repository: Repository by inject()
    private val notificationManager: NotificationWrapper by inject()
    private val syncManager: SyncManager by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var notificationId = notificationManager.reserveId()
    private var notificationBuilder: NotificationBuilder? = null
    private var currentMetadata: MediaMetadata? = null
    private var currentState: PlaybackState? = null
    private var sentEvent: Boolean = false

    private fun updateNotification(event: Event) {
        //Logger.d(TAG) { "updateNotification" }
        if (event.isPlaying) {
            notificationBuilder = { context ->
                setContentTitle(event.track)
                setContentText(event.artist)
                setSmallIcon(R.drawable.ic_stat_name)
                setShowWhen(false)

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

//                val deleteIntent = Intent(context, NotifListenerService::class.java)
//                deleteIntent.putExtra("org.reas.tracker2.appId", appId)
//                val deletePendingIntent = PendingIntent.getService(context, 42, deleteIntent,
//                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
//                setDeleteIntent(deletePendingIntent)
            }
        } else {
            notificationBuilder = null
        }
        showNotification()
    }

    private fun addEvent() {
        if (currentMetadata == null || currentState == null)
            return
        val metadata = currentMetadata!!
        val state = currentState!!
        if (metadata.artist == "" || metadata.title == "" || state.state == PlaybackState.STATE_NONE)
            return

        if (sentEvent) return
        sentEvent = true

        val event = Event(
            metadata = Metadata(
                info = TrackWithOptionalAlbum(
                    _track = Track(
                        title = metadata.title,
                        artist = metadata.artist
                    ),
                    _album = if (metadata.album != null) Album(
                        title = metadata.album,
                        artist = metadata.albumArtist ?: metadata.artist
                    ) else null,
                ),
                duration = metadata.duration.milliseconds
            ),
            timestamp = Instant.fromEpochMilliseconds(
                state.lastPositionUpdateTime - SystemClock.elapsedRealtime() + System.currentTimeMillis()
            ),
            position = state.position.milliseconds,
            isPlaying = state.state == PlaybackState.STATE_PLAYING,
            source = Source.local(appId)
        )
        scope.launch {
            repository.insertEvent(event)
            repository.insertEventInQueue(event)
            syncManager.submitEvent(event)
        }
        updateNotification(event)
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


    fun onConnect(controller: MediaController) {
        controller.metadata?.let { onMetadataChanged(it) }
        controller.playbackState?.let { onPlaybackStateChanged(it) }
    }

    override fun onMetadataChanged(metadata: MediaMetadata?) {
        Logger.d(TAG) { "onMetadataChanged($appId) $metadata" }

        if (metadata == null) return
        currentMetadata = metadata
        addEvent()
    }

    override fun onPlaybackStateChanged(state: PlaybackState?) {
        Logger.d(TAG) { "onPlaybackStateChanged($appId) $state" }

        if (state == null) return
        currentState = state
        sentEvent = false
        addEvent()
    }

    fun onDisconnect() {
        Logger.d(TAG) { "onDisconnect($appId)" }
        currentState = currentState?.let {
            PlaybackState.Builder(it).setState(
                PlaybackState.STATE_STOPPED,
                it.position + SystemClock.elapsedRealtime() - it.lastPositionUpdateTime,
                1.0f
            ).build()
        }
        sentEvent = false
        addEvent()
    }

//    fun onNotificationDismissed() {
//        notificationId = notificationManager.reserveId()
//        showNotification()
//    }
}

private class SessionListener: MediaSessionManager.OnActiveSessionsChangedListener, KoinComponent {
    private val settings: Settings by inject()
    private val controllers = mutableMapOf<String, MediaController>()
    private var callbacks = mutableMapOf<String, MediaCallback>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            settings.collect(isScrobblingEnabled) { value ->
                if (value) {
                    Logger.d(TAG) { "scrobbling enabled via settings" }
                    callbacks.forEach { (appId, callback) ->
                        val controller = controllers[appId]!!
                        withContext(Dispatchers.Main) {
                            controller.registerCallback(callback)
                            callback.onConnect(controller)
                        }
                    }
                } else {
                    Logger.d(TAG) { "scrobbling disabled via settings" }
                    callbacks.forEach { (appId, callback) ->
                        val controller = controllers[appId]!!
                        withContext(Dispatchers.Main) {
                            callback.onDisconnect()
                            controller.unregisterCallback(callback)
                        }
                    }
                }
            }
        }
    }

    override fun onActiveSessionsChanged(ctrl: List<MediaController>?) {
        Logger.d(TAG) { "onActiveSessionsChanged $ctrl" }
        if (ctrl == null) return

        val oldControllers = controllers.keys
        val newControllerMap = ctrl.associateBy { it.packageName }
        val newControllers = newControllerMap.keys

        val isScrobbling = settings.get(isScrobblingEnabled)

        // rewire callbacks for any refreshed controllers with the same package name
        // (happens e.g. if the service gets restarted)
        oldControllers.intersect(newControllers).forEach { appId ->
            if (controllers[appId] != newControllerMap[appId]) {
                val callback = callbacks[appId]!!
                if (isScrobbling) {
                    controllers[appId]!!.unregisterCallback(callback)
                    newControllerMap[appId]!!.registerCallback(callback)
                }
                controllers[appId] = newControllerMap[appId]!!
            }
        }

        // remove callbacks for disconnected session controllers
        oldControllers.minus(newControllers).forEach { appId ->
            val callback = callbacks[appId]!!
            if (isScrobbling) {
                callback.onDisconnect()
                controllers[appId]!!.unregisterCallback(callback)
            }
            callbacks.remove(appId)
            controllers.remove(appId)
        }

        // add callbacks for new session controllers
        newControllers.minus(oldControllers).forEach { appId ->
            val callback = MediaCallback(appId)
            val controller = newControllerMap[appId]!!
            if (isScrobbling) {
                controller.registerCallback(callback)
                callback.onConnect(controller)
            }
            callbacks[appId] = callback
            controllers[appId] = controller
        }
    }

//    fun onNotificationDismissed(appId: String) {
//        Logger.d(TAG) { "onNotificationDismissed($appId)" }
//        callbacks[appId]?.onNotificationDismissed()
//    }
}

class NotifListenerService: NotificationListenerService(), KoinComponent {
    private var initialized = false
    private var listener: SessionListener? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        if (startId == 42)
//            listener!!.onNotificationDismissed(intent!!.getStringExtra("org.reas.tracker2.appId")!!)
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
        Logger.d(TAG) { "onListenerConnected" }
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
}
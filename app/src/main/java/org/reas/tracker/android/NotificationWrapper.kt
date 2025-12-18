package org.reas.tracker.android

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat

object NotificationWrapper {
    private var notificationManager: NotificationManager? = null
    private val channels = hashMapOf<String, String>()
    private var nextId = 0

    fun init(notificationManager: NotificationManager) {
        this.notificationManager = notificationManager
    }

    fun createChannel(name: String, importance: Int, params: NotificationChannel.() -> Unit = {}) {
        val channelIdString = channels.size.toString()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelIdString, name, importance).apply(params)
            notificationManager!!.createNotificationChannel(channel)
            channels[name] = channelIdString
        }
    }

    fun deletePreviousChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager!!.notificationChannels.forEach { channel ->
                notificationManager!!.deleteNotificationChannel(channel.id)
            }
        }
    }

    fun reserveId() = nextId++

    fun show(context: Context, channel: String, id: Int? = null, params: Notification.Builder.() -> Unit): Int {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("MessagingService", "Notifications permission not granted")
        }
        val notificationId = id ?: nextId++
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Notification.Builder(context, channels[channel]).apply(params).build()
        else
            Notification.Builder(context).apply(params).build()
        if (notificationManager != null) {
            notificationManager!!.notify(notificationId, notification)
        }
        return notificationId
    }

    fun hide(id: Int) {
        if (notificationManager != null)
            notificationManager!!.cancel(id)
    }
}
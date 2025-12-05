package org.reas.tracker.firebase

import android.Manifest
import android.R.string.no
import android.app.Notification
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.reas.tracker.NotificationWrapper
import org.reas.tracker.R


class MessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        message.notification?.let { notification ->

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            NotificationWrapper.sendNotification(this, notification.title!!) {
                setContentTitle(notification.title)
                setContentText(notification.body)
                setAutoCancel(true)
                setSound(defaultSoundUri)
                setSmallIcon(R.drawable.ic_launcher_foreground)
            }
        }
    }
}
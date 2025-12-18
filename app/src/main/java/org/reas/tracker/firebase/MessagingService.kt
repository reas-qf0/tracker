package org.reas.tracker.firebase

import android.media.RingtoneManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.reas.tracker.android.NotificationWrapper
import org.reas.tracker.R


class MessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        message.notification?.let { notification ->

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            NotificationWrapper.show(this, notification.title!!) {
                setContentTitle(notification.title)
                setContentText(notification.body)
                setAutoCancel(true)
                setSound(defaultSoundUri)
                setSmallIcon(R.drawable.ic_launcher_foreground)
            }
        }
    }
}
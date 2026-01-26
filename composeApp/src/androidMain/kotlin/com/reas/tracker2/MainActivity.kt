package com.reas.tracker2

import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.reas.tracker2.android.DailyReportWorker
import com.reas.tracker2.android.NotificationWrapper
import com.reas.tracker2.ui.TrackerApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationWrapper.init(getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        NotificationWrapper.deletePreviousChannels()
        NotificationWrapper.createChannel("Now Playing", 2)
        NotificationWrapper.createChannel("Sync Indicator", 0)
        NotificationWrapper.createChannel("Daily Report", 1)

        DailyReportWorker.start(this)

        setContent {
            TrackerApp(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
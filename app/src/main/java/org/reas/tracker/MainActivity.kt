package org.reas.tracker

import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.reas.tracker.android.DailyReportWorker
import org.reas.tracker.android.FirestoreSyncWorker
import org.reas.tracker.android.NotificationWrapper
import org.reas.tracker.ui.TrackerApp

class MainActivity : ComponentActivity() {
    private val container = TrackerApplication.instance!!.container
    private val authManager = container.authManager
    private val cloudSave = container.cloudSave

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationWrapper.init(getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        NotificationWrapper.deletePreviousChannels()
        NotificationWrapper.createChannel("Now Playing", 2)
        NotificationWrapper.createChannel("Sync Indicator", 0)
        NotificationWrapper.createChannel("Daily Report", 1)

        authManager.init(this)
        authManager.onSignIn { user ->
            cloudSave.setId(user.uid)
            cloudSave.submitBatchEvents()
            cloudSave.trackRemoteEvents()
        }
        authManager.onSignOut {
            cloudSave.onSignOut()
        }
        savedInstanceState?.run {
            authManager.restore(
                getParcelable("user"),
                getBoolean("signedIn")
            )
        }

        FirestoreSyncWorker.start(container)
        DailyReportWorker.start(container)

        setContent {
            TrackerApp(
                authManager,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            authManager.signInOnLaunch()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("user", authManager.user)
        outState.putBoolean("signedIn", authManager.signedIn)
    }
}
package org.reas.tracker

import android.app.NotificationManager
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import org.reas.tracker.ui.TrackerApp
import org.reas.tracker.ui.theme.TrackerTheme

class MainActivity : ComponentActivity() {
    private val container = TrackerApplication.instance!!.container
    private val authManager = container.authManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        authManager.init(this)
        savedInstanceState?.run {
            authManager.restore(
                getParcelable("user"),
                getBoolean("signedIn")
            )
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { regTokenTask ->
            if (regTokenTask.isSuccessful) {
                Log.d("FirebaseMessaging", "FCM registration token: ${regTokenTask.result}")
            } else {
                Log.e("FirebaseMessaging", "Unable to retrieve registration token",
                    regTokenTask.exception)
            }
        }
        FirebaseInstallations.getInstance().id.addOnCompleteListener { installationIdTask ->
            if (installationIdTask.isSuccessful) {
                Log.d("FirebaseInstallations", "Firebase Installations ID: ${installationIdTask.result}")
            } else {
                Log.e("FirebaseInstallations", "Unable to retrieve installations ID",
                    installationIdTask.exception)
            }
        }

        NotificationWrapper.init(getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        NotificationWrapper.createChannel("Channel 1", 0) {
            description = "Channel description 1"
        }
        NotificationWrapper.createChannel("Channel 2", 0) {
            description = "Channel description 2"
        }

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
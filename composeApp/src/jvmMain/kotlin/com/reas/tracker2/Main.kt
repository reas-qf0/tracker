package com.reas.tracker2

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.reas.tracker2.network.SyncManager
import com.reas.tracker2.ui.TrackerApp
import org.koin.compose.koinInject

fun main() {
    startKoinMp {
        printLogger()
    }

    application {
        val syncManager: SyncManager = koinInject()
        LaunchedEffect(Unit) {
            syncManager.establishConnection()
        }
        Window(
            onCloseRequest = ::exitApplication,
            title = "Tracker2",
        ) {
            TrackerApp()
        }
    }
}
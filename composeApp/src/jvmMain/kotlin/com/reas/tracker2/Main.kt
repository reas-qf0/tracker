package com.reas.tracker2

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.reas.tracker2.ui.TrackerApp

fun main() {
    startKoinMp {
        printLogger()
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Tracker2",
        ) {
            TrackerApp(modifier = Modifier.fillMaxSize())
        }
    }
}
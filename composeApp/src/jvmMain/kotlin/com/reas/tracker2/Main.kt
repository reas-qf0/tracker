package com.reas.tracker2

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.reas.tracker2.ui.TrackerApp
import org.koin.core.context.GlobalContext.startKoin

fun main() {
    startKoin {
        printLogger()
        modules(sharedModule, platformModule)
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
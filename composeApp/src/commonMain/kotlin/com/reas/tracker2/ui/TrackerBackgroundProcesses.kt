package com.reas.tracker2.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.reas.tracker2.network.SyncManager
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.seconds

@Composable
fun TrackerBackgroundProcesses() {
    val syncManager: SyncManager = koinInject()

    LaunchedEffect(Unit) {
        while (true) {
            if (!syncManager.establishConnection())
                delay(15.seconds)
        }
    }
}
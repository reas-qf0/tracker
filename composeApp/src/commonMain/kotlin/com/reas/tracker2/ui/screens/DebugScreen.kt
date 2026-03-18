package com.reas.tracker2.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reas.tracker2.ui.navigation.ApplicationState
import com.reas.tracker2.ui.viewmodels.DebugScreenViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebugScreen(
    applicationState: ApplicationState,
    modifier: Modifier = Modifier,
    viewModel: DebugScreenViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val eventCount by remember { viewModel.eventCount }.collectAsStateWithLifecycle()
    val unprocessedEventCount by remember { viewModel.unprocessedEventCount }.collectAsStateWithLifecycle()
    val unsyncedEventCount by remember { viewModel.unsyncedEventCount }.collectAsStateWithLifecycle()
    val playCount by remember { viewModel.playCount }.collectAsStateWithLifecycle()

    applicationState.setTitle("Debug")
    Column(modifier = modifier) {
        Text("Local events: $eventCount")
        Text("Unprocessed events: $unprocessedEventCount")
        Text("Unsynced events: $unsyncedEventCount")
        Text("Total plays: $playCount")
        Button(onClick = { scope.launch { viewModel.refreshApiKey() } }) {
            Text("Refresh API key")
        }
        Button(onClick = { scope.launch { viewModel.flushAllEvents() } }) {
            Text("Sync all events")
        }
    }
}
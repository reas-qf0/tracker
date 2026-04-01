package com.reas.tracker2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.reas.tracker2.ui.navigation.ApplicationState
import com.reas.tracker2.ui.rememberAsState
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
    val eventCount by rememberAsState { viewModel.eventCount }
    val unprocessedEventCount by rememberAsState { viewModel.unprocessedEventCount }
    val unsyncedEventCount by rememberAsState { viewModel.unsyncedEventCount }
    val playCount by rememberAsState { viewModel.playCount }
    val mediaEventLog by rememberAsState { viewModel.mediaEventLog }

    val verticalScrollState = rememberScrollState()
    LaunchedEffect(mediaEventLog) {
        verticalScrollState.scrollTo(Int.MAX_VALUE)
    }

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
        Text("Event log:")
        Text(
            text = mediaEventLog,
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            fontFamily = FontFamily.Monospace,
            lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.2,
            modifier = Modifier.fillMaxSize()
                .padding(10.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.medium)
                .verticalScroll(verticalScrollState)
                .horizontalScroll(rememberScrollState())
                .padding(10.dp)
        )
    }
}
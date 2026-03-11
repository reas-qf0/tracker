package com.reas.tracker2.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.reas.tracker2.ui.navigation.TrackerNavScaffold
import com.reas.tracker2.ui.theme.TrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerApp(
    modifier: Modifier = Modifier,
) {
    TrackerBackgroundProcesses()

    TrackerTheme {
        TrackerNavScaffold(modifier)
    }
}
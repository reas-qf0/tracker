package com.reas.tracker2.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.compose.rememberNavController
import com.reas.tracker2.ui.navigation.TrackerNavHost
import com.reas.tracker2.ui.navigation.TrackerNavigationPane
import com.reas.tracker2.ui.theme.TrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerApp(
    modifier: Modifier = Modifier,
) {
    val controller = rememberNavController()
    val title = remember { mutableStateOf("") }
    var canNavigateBack by remember { mutableStateOf(false) }
    controller.addOnDestinationChangedListener { _, _, _ ->
        canNavigateBack = controller.previousBackStackEntry != null
    }

    TrackerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            title.value,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        ) },
                    navigationIcon = {
                        IconButton(
                            onClick = { controller.popBackStack() },
                            enabled = canNavigateBack
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Localized description"
                            )
                        }
                    }
                )
            },
            contentWindowInsets = WindowInsets(),
            modifier = modifier
        ) { innerPadding ->
            TrackerNavigationPane(controller, modifier = Modifier.padding(innerPadding)) {
                TrackerNavHost(
                    title = title,
                    navController = controller
                )
            }
        }
    }
}
package org.reas.tracker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.reas.tracker.firebase.AuthManager
import org.reas.tracker.ui.theme.TrackerTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseUser
import org.reas.tracker.R
import org.reas.tracker.ui.navigation.TrackerNavHost
import org.reas.tracker.ui.navigation.TrackerNavigationPane
import org.reas.tracker.ui.theme.Typography

@Composable
fun TrackerApp(
    authManager: AuthManager,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }

    TrackerTheme {
        Scaffold(modifier = modifier) { innerPadding ->
            val topBarPadding = PaddingValues(
                start = innerPadding.calculateLeftPadding(LocalLayoutDirection.current),
                end = innerPadding.calculateRightPadding(LocalLayoutDirection.current)
            )

            ShowIf(loading) {
                Loading(modifier = Modifier.padding(innerPadding).fillMaxSize())
            }

            ShowIf(authManager.signedIn && !loading) {
                SignedIn(
                    user = authManager.user!!,
                    signOut = {
                        loading = true
                        scope.launch {
                            authManager.signOut()
                            loading = false
                        }
                    },
                    modifier = Modifier.padding(topBarPadding)
                )
            }

            ShowIf(!authManager.signedIn && !loading) {
                SignedOut(
                    signIn = {
                        loading = true
                        scope.launch {
                            authManager.signIn()
                            loading = false
                        }
                    },
                    modifier = Modifier.padding(innerPadding).fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun ShowIf(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignedIn(
    user: FirebaseUser,
    signOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val controller = rememberNavController()
    val title = remember { mutableStateOf("") }
    var canNavigateBack by remember { mutableStateOf(false) }
    controller.addOnDestinationChangedListener { _, _, _ ->
        canNavigateBack = controller.previousBackStackEntry != null
    }

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
                user = user,
                signOut = signOut,
                navController = controller
            )
        }
    }
}

@Composable
private fun SignedOut(signIn: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.signed_out))
            Spacer(Modifier.height(5.dp))
            Button(onClick = signIn) {
                Text(stringResource(R.string.sign_in_with_google))
            }
        }
    }
}

@Composable
private fun Loading(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.loading),
                style = Typography.displaySmall
            )
        }
    }
}
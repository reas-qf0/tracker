package org.reas.tracker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.reas.tracker.firebase.AuthManager

@Composable
fun TrackerApp(
    authManager: AuthManager,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    Column {
        if (authManager.user != null) {
            Text("Welcome ${authManager.user!!.displayName}!")
            Button(onClick = {
                scope.launch { authManager.signOut() }
            }) {
                Text("Sign out")
            }
            Button(onClick = {
                throw RuntimeException("Hello Crashlytics")
            }) {
                Text("Crash app")
            }
        }
        else {
            Text("You're not logged in")
            Button(onClick = {
                scope.launch { authManager.signIn() }
            }) {
                Text("Sign in")
            }
        }
    }
}
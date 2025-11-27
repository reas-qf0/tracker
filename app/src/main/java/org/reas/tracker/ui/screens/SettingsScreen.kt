package org.reas.tracker.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.reas.tracker.R

@Composable
fun SettingsScreen(
    signOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = signOut,
        modifier = modifier
    ) {
        Text(stringResource(R.string.sign_out))
    }
}
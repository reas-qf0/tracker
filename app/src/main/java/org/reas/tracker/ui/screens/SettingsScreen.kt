package org.reas.tracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.reas.tracker.R
import org.reas.tracker.BuildConfig

@Composable
fun SettingsScreen(
    signOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsScreenViewModel = viewModel()
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(horizontal = 10.dp)
    ) {
        Button(
            onClick = signOut,
            modifier = modifier
        ) {
            Text(stringResource(R.string.sign_out))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Current version: ${BuildConfig.VERSION_NAME}")
            Spacer(Modifier.weight(1.0F))
            Button(
                enabled = viewModel.updateState.action != null,
                onClick = viewModel.updateState.action ?: {}
            ) {
                Text(viewModel.updateState.text)
            }
        }
    }
}
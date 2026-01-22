package org.reas.tracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.reas.tracker.R
import org.reas.tracker.BuildConfig
import org.reas.tracker.ui.viewmodels.SettingsScreenViewModel

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
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val scrobblingEnabled by remember { viewModel.isScrobblingEnabled() }.collectAsState()
            Text(stringResource(R.string.enable_tracking))
            Spacer(Modifier.weight(1.0F))
            Switch(
                checked = scrobblingEnabled,
                onCheckedChange = { viewModel.setScrobblingEnabled(it) }
            )
        }

        Button(
            onClick = { viewModel.restartService() },
            modifier = modifier
        ) {
            Text(stringResource(R.string.restart_service))
        }

        Button(
            onClick = signOut,
            modifier = modifier
        ) {
            Text(stringResource(R.string.sign_out))
        }

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.current_version, BuildConfig.VERSION_NAME))
            Spacer(Modifier.weight(1.0F))
            Button(
                enabled = viewModel.updateState.action != null,
                onClick = viewModel.updateState.action ?: {}
            ) {
                Text(stringResource(viewModel.updateState.text))
            }
        }

        Button(
            enabled = viewModel.reviewState.action != null,
            onClick = viewModel.reviewState.action ?: {}
        ) {
            Text(stringResource(viewModel.reviewState.text))
        }
    }
}
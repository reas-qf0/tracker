package com.reas.tracker2.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reas.tracker2.ui.derivedState
import com.reas.tracker2.ui.navigation.ApplicationState
import com.reas.tracker2.ui.state
import com.reas.tracker2.ui.viewmodels.LoginDialogViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginDialog(
    applicationState: ApplicationState,
    modifier: Modifier = Modifier,
    viewModel: LoginDialogViewModel = koinViewModel()
) {
    var hostName by state(viewModel.hostName)
    var port by state(viewModel.port.toString())
    var userName by state(viewModel.userName)

    val canSave by derivedState { port != "" && userName != "" && hostName != "" }

    Column(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.background,
            shape = MaterialTheme.shapes.medium
        ).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = hostName,
            onValueChange = { hostName = it },
            maxLines = 1,
            label = { Text("Server hostname") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = port,
            onValueChange = { input ->
                port = input.filter { it.isDigit() }.let {
                    if (it == "") it else
                        it.toInt().coerceIn(0..65535).toString()
                }
            },
            maxLines = 1,
            label = { Text("Server port") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            maxLines = 1,
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.padding(12.dp)) {
            Button(
                enabled = canSave,
                onClick = {
                    viewModel.setValues(hostName, port.toInt(), userName)
                    applicationState.goBack()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
            Spacer(Modifier.width(70.dp))
            Button(
                onClick = {
                    applicationState.goBack()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Cancel")
            }
        }
    }
}
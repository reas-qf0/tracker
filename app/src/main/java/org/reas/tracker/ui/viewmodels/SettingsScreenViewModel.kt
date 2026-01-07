package org.reas.tracker.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.reas.tracker.TrackerApplication
import org.reas.tracker.android.DataStoreWrapper.Companion.SCROBBLING_ENABLED
import org.reas.tracker.rustore.CheckStatus
import org.reas.tracker.rustore.UpdateStatus
import ru.rustore.sdk.appupdate.model.AppUpdateInfo
import kotlin.math.roundToInt

data class UpdateState(
    val text: String,
    val action: (() -> Unit)? // null means button is disabled
)

class SettingsScreenViewModel : ViewModel() {
    private val updateManager = TrackerApplication.instance!!.container.updateManager
    private val preferences = TrackerApplication.instance!!.container.preferences

    var updateState by mutableStateOf(UpdateState(
        text = "Check for updates",
        action = { checkForUpdates() }
    ))

    fun checkForUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            updateManager.checkForUpdates().collect { status ->
                updateState = when (status) {
                    is CheckStatus.Available -> UpdateState(
                        text = "Install update",
                        action = { downloadUpdate(status.updateInfo) }
                    )
                    is CheckStatus.Checking -> UpdateState(
                        text = "Checking for updates...",
                        action = null
                    )
                    is CheckStatus.Failed -> UpdateState(
                        text = "Failed to check",
                        action = { checkForUpdates() }
                    )
                    is CheckStatus.Latest -> UpdateState(
                        text = "No updates",
                        action = { checkForUpdates() }
                    )
                }
            }
        }
    }

    fun downloadUpdate(updateInfo: AppUpdateInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            updateManager.update(updateInfo).collect { status ->
                updateState = when (status) {
                    is UpdateStatus.Downloading -> UpdateState(
                        text = "Downloading... (${(status.progress * 100).roundToInt()})%",
                        action = null
                    )
                    is UpdateStatus.Failed -> UpdateState(
                        text = "Failed to update",
                        action = { downloadUpdate(updateInfo) }
                    )
                    is UpdateStatus.Ready -> UpdateState(
                        text = "Installing...",
                        action = null
                    )
                }
            }
        }
    }

    fun isScrobblingEnabled() = preferences.get(SCROBBLING_ENABLED)
        .map { it ?: true }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = true
        )

    fun setScrobblingEnabled(x: Boolean) {
        viewModelScope.launch {
            preferences.set(SCROBBLING_ENABLED, x)
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
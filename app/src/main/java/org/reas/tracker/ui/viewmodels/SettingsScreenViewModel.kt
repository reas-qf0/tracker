package org.reas.tracker.ui.viewmodels

import android.content.Intent
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
import org.reas.tracker.R
import org.reas.tracker.TrackerApplication
import org.reas.tracker.android.DataStoreWrapper.Companion.SCROBBLING_ENABLED
import org.reas.tracker.android.NotifListenerService
import org.reas.tracker.rustore.CheckStatus
import org.reas.tracker.rustore.ReviewStatus
import org.reas.tracker.rustore.UpdateStatus
import ru.rustore.sdk.appupdate.model.AppUpdateInfo

data class ButtonState(
    val text: Int,
    val action: (() -> Unit)? // null means button is disabled
)

class SettingsScreenViewModel : ViewModel() {
    private val updateManager = TrackerApplication.instance!!.container.updateManager
    private val reviewManager = TrackerApplication.instance!!.container.reviewManager
    private val preferences = TrackerApplication.instance!!.container.preferences
    private val context = TrackerApplication.instance!!.container.context

    var updateState by mutableStateOf(ButtonState(
        text = R.string.check_for_updates,
        action = { checkForUpdates() }
    ))

    var reviewState by mutableStateOf(ButtonState(
        text = R.string.leave_a_review,
        action = { review() }
    ))

    fun checkForUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            updateManager.checkForUpdates().collect { status ->
                updateState = when (status) {
                    is CheckStatus.Available -> ButtonState(
                        text = R.string.install_update,
                        action = { downloadUpdate(status.updateInfo) }
                    )
                    is CheckStatus.Checking -> ButtonState(
                        text = R.string.checking_for_updates,
                        action = null
                    )
                    is CheckStatus.Failed -> ButtonState(
                        text = R.string.failed_to_check,
                        action = { checkForUpdates() }
                    )
                    is CheckStatus.Latest -> ButtonState(
                        text = R.string.no_updates,
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
                    is UpdateStatus.Downloading -> ButtonState(
                        text = R.string.downloading,
                        action = null
                    )
                    is UpdateStatus.Failed -> ButtonState(
                        text = R.string.failed_to_update,
                        action = { downloadUpdate(updateInfo) }
                    )
                    is UpdateStatus.Ready -> ButtonState(
                        text = R.string.installing,
                        action = null
                    )
                    is UpdateStatus.Canceled -> ButtonState(
                        text = R.string.update_canceled,
                        action = { downloadUpdate(updateInfo) }
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

    fun restartService() {
        context.startService(Intent(context, NotifListenerService::class.java))
    }

    fun review() {
        viewModelScope.launch(Dispatchers.IO) {
            reviewManager.review().collect { status ->
                reviewState = when (status) {
                    is ReviewStatus.Requesting -> ButtonState(
                        text = R.string.requesting,
                        action = null
                    )
                    is ReviewStatus.RequestError -> ButtonState(
                        text = R.string.request_failed,
                        action = { review() }
                    )
                    is ReviewStatus.Launching -> ButtonState(
                        text = R.string.launching,
                        action = null
                    )
                    is ReviewStatus.LaunchError -> ButtonState(
                        text = R.string.launch_failed,
                        action = { review() }
                    )
                    is ReviewStatus.Complete -> ButtonState(
                        text = R.string.thank_you,
                        action = { review() }
                    )
                }
            }
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
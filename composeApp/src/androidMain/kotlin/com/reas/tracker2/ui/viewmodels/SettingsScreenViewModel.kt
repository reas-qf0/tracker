package com.reas.tracker2.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.reas.tracker2.TrackerApplication
import com.reas.tracker2.android.DataStoreWrapper.Companion.SCROBBLING_ENABLED

class SettingsScreenViewModel : ViewModel() {
    private val preferences = TrackerApplication.instance!!.container.preferences

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
package com.reas.tracker2.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reas.tracker2.settings.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsScreenViewModel(private val settings: Settings) : ViewModel() {
    val scrobblingEnabledFlow = settings.flow(isScrobblingEnabled).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = settings[isScrobblingEnabled]
    )

    fun setScrobblingEnabled(value: Boolean) {
        viewModelScope.launch {
            settings[isScrobblingEnabled] = value
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
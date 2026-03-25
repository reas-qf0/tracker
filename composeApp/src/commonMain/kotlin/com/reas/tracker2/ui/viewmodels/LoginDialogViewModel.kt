package com.reas.tracker2.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reas.tracker2.settings.*
import kotlinx.coroutines.launch

class LoginDialogViewModel(
    private val settings: Settings
) : ViewModel() {
    val hostName
        get() = settings[instanceHostName]
    val port
        get() = settings[instancePort]
    val userName
        get() = settings[username]

    fun setValues(hostName: String, port: Int, userName: String) {
        viewModelScope.launch {
            settings.update {
                set(instanceHostName, hostName)
                set(instancePort, port)
                set(username, userName)
            }
        }
    }
}
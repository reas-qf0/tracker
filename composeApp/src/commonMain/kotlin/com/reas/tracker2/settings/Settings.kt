package com.reas.tracker2.settings

import androidx.datastore.preferences.core.booleanPreferencesKey

val isScrobblingEnabled = Setting(
    key = booleanPreferencesKey("isScrobblingEnabled"),
    defaultValue = true
)
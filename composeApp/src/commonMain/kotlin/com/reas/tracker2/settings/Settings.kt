package com.reas.tracker2.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

val isScrobblingEnabled = Setting(
    key = booleanPreferencesKey("isScrobblingEnabled"),
    defaultValue = true
)

val instanceHostName = Setting(
    key = stringPreferencesKey("instanceHostName"),
    defaultValue = ""
)
val instancePort = Setting(
    key = intPreferencesKey("instancePort"),
    defaultValue = 0
)
val username = Setting(
    key = stringPreferencesKey("username"),
    defaultValue = ""
)

val lastSeenId = Setting(
    key = longPreferencesKey("lastSeenId"),
    defaultValue = 0
)
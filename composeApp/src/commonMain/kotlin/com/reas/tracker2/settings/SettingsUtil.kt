package com.reas.tracker2.settings

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.reas.tracker2.util.PlatformDependentPaths
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath

data class Setting<T>(
    val key: Preferences.Key<T>,
    val defaultValue: T
)

typealias Settings = DataStore<Preferences>
fun createDataStore(pathProvider: PlatformDependentPaths): Settings =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { pathProvider.getPreferencesPath().toPath() },
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
    )

internal const val dataStoreFileName = "tracker2.preferences_pb"


fun<T> Settings.flow(setting: Setting<T>) =
    data.map { it[setting.key] ?: setting.defaultValue }.distinctUntilChanged()

suspend fun<T> Settings.collect(setting: Setting<T>, collector: suspend (T) -> Unit) = flow(setting).drop(1).collect(collector)

operator fun<T> Settings.get(setting: Setting<T>) = runBlocking { flow(setting).first() }

suspend fun<T> Settings.setAsync(setting: Setting<T>, value: T) =
    updateData {
        it.toMutablePreferences().also { preferences ->
            preferences[setting.key] = value
        }
    }

suspend fun Settings.update(block: MutablePreferences.() -> Unit) =
    updateData {
        it.toMutablePreferences().also(block)
    }

fun<T> MutablePreferences.set(setting: Setting<T>, value: T) {
    this[setting.key] = value
}

suspend fun<T> Settings.resetAsync(setting: Setting<T>) = setAsync(setting, setting.defaultValue)

fun<T> Settings.set(setting: Setting<T>, value: T) = runBlocking { setAsync(setting, value) }

fun<T> Settings.reset(setting: Setting<T>) = runBlocking { resetAsync(setting) }
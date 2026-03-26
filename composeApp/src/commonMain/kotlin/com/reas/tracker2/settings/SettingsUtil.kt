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
data class SerializableSetting<K, T>(
    val key: Preferences.Key<K>,
    val defaultValue: T,
    val encode: (T) -> K,
    val decode: (K) -> T
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
fun<K, T> Settings.flow(setting: SerializableSetting<K, T>) =
    data.map {
        val value = it[setting.key]
        if (value == null) setting.defaultValue else setting.decode(value)
    }.distinctUntilChanged()

suspend fun<T> Settings.collect(setting: Setting<T>, collector: suspend (T) -> Unit) = flow(setting).drop(1).collect(collector)
suspend fun<K, T> Settings.collect(setting: SerializableSetting<K, T>, collector: suspend (T) -> Unit) = flow(setting).drop(1).collect(collector)

operator fun<T> Settings.get(setting: Setting<T>) = runBlocking { flow(setting).first() }
operator fun<K, T> Settings.get(setting: SerializableSetting<K, T>) = runBlocking { flow(setting).first() }

suspend fun Settings.update(block: MutablePreferences.() -> Unit) =
    updateData {
        it.toMutablePreferences().also(block)
    }
fun<T> MutablePreferences.set(setting: Setting<T>, value: T) {
    this[setting.key] = value
}

suspend operator fun<T> Settings.set(setting: Setting<T>, value: T) =
    update {
        this[setting.key] = value
    }
suspend operator fun<K, T> Settings.set(setting: SerializableSetting<K, T>, value: T) =
    update {
        this[setting.key] = setting.encode(value)
    }

suspend fun<T> Settings.reset(setting: Setting<T>) = set(setting, setting.defaultValue)
suspend fun<K, T> Settings.reset(setting: SerializableSetting<K, T>) = update {
    remove(setting.key)
}

fun<T> Settings.setBlocking(setting: Setting<T>, value: T) = runBlocking { set(setting, value) }
fun<K, T> Settings.setBlocking(setting: SerializableSetting<K, T>, value: T) = runBlocking { set(setting, value) }


fun<T> Settings.resetBlocking(setting: Setting<T>) = runBlocking { reset(setting) }
fun<K, T> Settings.resetBlocking(setting: SerializableSetting<K, T>) = runBlocking { reset(setting) }
package com.reas.tracker2.android

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlin.text.get

class DataStoreWrapper(private val context: Context) {
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    fun<T> get(key: Preferences.Key<T>): Flow<T?> =
        context.dataStore.data.map { preferences -> preferences[key] }

    fun<T> get(key: Preferences.Key<T>, default: T): Flow<T> =
        context.dataStore.data.map { preferences -> preferences[key] ?: default }

    fun<T> getValue(key: Preferences.Key<T>): T? =
        runBlocking { get(key).first() }

    fun<T> getValue(key: Preferences.Key<T>, default: T): T =
        runBlocking { get(key, default).first() }

    suspend fun<T> set(key: Preferences.Key<T>, value: T) {
        context.dataStore.updateData {
            it.toMutablePreferences().also { preferences ->
                preferences[key] = value
            }
        }
    }

    companion object {
        val SCROBBLING_ENABLED = booleanPreferencesKey("scrobbling_enabled")
    }
}
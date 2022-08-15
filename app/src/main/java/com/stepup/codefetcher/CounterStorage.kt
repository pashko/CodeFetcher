package com.stepup.codefetcher

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CounterStorage @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val currentValue: Flow<Int> = dataStore.data.map { it.counter }

    suspend fun increment() = dataStore.edit { it.counter++ }
}

private val CounterKey = intPreferencesKey("counter")

private val Preferences.counter get() = this[CounterKey] ?: 0

private var MutablePreferences.counter
    get() = (this as Preferences).counter
    set(value) { this[CounterKey] = value }
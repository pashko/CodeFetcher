package com.stepup.codefetcher

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.rxjava3.RxDataStore
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class CounterStorage @Inject constructor(
    private val dataStore: RxDataStore<Preferences>
) {

    val currentValue: Observable<Int> = dataStore.data()
        .map { it[CounterKey] ?: 0 }.toObservable()

    fun increment(): Completable = Completable.fromSingle(
        dataStore.updateDataAsync {
            Single.just(it.toMutablePreferences().apply { counter++ })
        }
    )
}

private val CounterKey = intPreferencesKey("counter")

private val Preferences.counter get() = this[CounterKey] ?: 0

private var MutablePreferences.counter
    get() = (this as Preferences).counter
    set(value) { this[CounterKey] = value }
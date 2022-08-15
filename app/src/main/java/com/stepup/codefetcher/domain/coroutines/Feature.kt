package com.stepup.codefetcher.domain.coroutines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

interface Feature {

    val featureScope: CoroutineScope

    fun <State, Event> Container.Mutator<State, Event>.perform(
        action: suspend Container.Mutator<State, Event>.(CoroutineScope) -> Unit
    ) {
        featureScope.launch { action(this) }
    }
}

abstract class FeatureViewModel : ViewModel(), Feature {

    override val featureScope = viewModelScope +
            CoroutineExceptionHandler { _, error -> onUnhandledError(error) }

    protected open fun onUnhandledError(e: Throwable) {}
}

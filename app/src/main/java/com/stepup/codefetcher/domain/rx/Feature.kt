package com.stepup.codefetcher.domain.rx

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface Feature {

    val disposable: CompositeDisposable

    fun <State, Event> Container.Mutator<State, Event>.perform(
        action: Container.Mutator<State, Event>.() -> Disposable
    ) {
        disposable += action()
    }
}

abstract class FeatureViewModel : ViewModel(), Feature {

    override val disposable = CompositeDisposable()

    override fun onCleared() = disposable.dispose()
}
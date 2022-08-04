package com.stepup.codefetcher

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import javax.inject.Inject

@HiltViewModel
class FetchCodeViewModel @Inject constructor(
    private val service: CodeService,
    private val counterStorage: CounterStorage,
): ViewModel() {

    private val disposable = CompositeDisposable()

    private val mutableCode = BehaviorSubject.createDefault(Maybe.empty<String>())
    private val mutableIsLoading = BehaviorSubject.createDefault(false)

    val state: Observable<State> = Observable.combineLatest(
        mutableCode,
        mutableIsLoading,
        counterStorage.currentValue
    ) { code, isLoading, counter -> State(code.blockingGet(), isLoading, counter) }

    private val mutableErrors = PublishSubject.create<Failure>()
    val errors: Observable<Failure> = mutableErrors

    fun fetchCode() {
        disposable += service.getResponseCode()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { mutableIsLoading.onNext(true) }
            .doOnTerminate { mutableIsLoading.onNext(false) }
            // we don't want increment() to be cancelled so not adding to the disposable
            .doOnTerminate { counterStorage.increment().subscribe() }
            .map { Maybe.just(it.response_code) }
            .subscribe(mutableCode::onNext, ::handleError)
    }

    private fun handleError(error: Throwable) {
        mutableErrors.onNext(Failure(error.errorMessage))
    }

    override fun onCleared() = disposable.dispose()

    data class Failure(val message: String? = null)

    data class State(
        val code: String? = null,
        val isLoading: Boolean,
        val counter: Int
    )
}

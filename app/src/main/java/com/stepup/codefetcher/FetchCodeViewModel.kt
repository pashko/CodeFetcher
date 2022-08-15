package com.stepup.codefetcher

import com.stepup.codefetcher.FetchCodeViewModel.Failure
import com.stepup.codefetcher.FetchCodeViewModel.State
import com.stepup.codefetcher.domain.rx.Container
import com.stepup.codefetcher.domain.rx.FeatureViewModel
import com.stepup.codefetcher.domain.rx.doOnLoading
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.rx3.asObservable
import kotlinx.coroutines.rx3.rxCompletable
import javax.inject.Inject

@HiltViewModel
class FetchCodeViewModel @Inject constructor(
    private val service: CodeService,
    private val counterStorage: CounterStorage,
): FeatureViewModel(), Container<State, Failure> {

    private val container = Container<_, Failure>(State())

    override val state = container.state
    override val events = container.events

    init {
        container.perform {
            counterStorage.currentValue.asObservable().subscribe {
                updateState { copy(counter = it) }
            }
        }
    }

    fun fetchCode() = container.perform {
        // we don't want increment() to be cancelled so not adding to the disposable
        rxCompletable { counterStorage.increment() }.subscribe()
        service.getResponseCode()
            .subscribeOn(Schedulers.io())
            .doOnLoading { updateState { copy(isLoading = it) } }
            .subscribeBy(
                onSuccess = { updateState { copy(code = it.response_code) } },
                onError = ::handleError
            )
    }

    private fun handleError(error: Throwable) {
        container.emitEvent(Failure(error.errorMessage))
    }

    data class Failure(val message: String? = null)

    data class State(
        val code: String? = null,
        val isLoading: Boolean = false,
        val counter: Int = 0
    )
}

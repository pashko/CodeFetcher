package com.stepup.codefetcher

import com.stepup.codefetcher.FetchCodeViewModel.Failure
import com.stepup.codefetcher.FetchCodeViewModel.State
import com.stepup.codefetcher.domain.coroutines.Container
import com.stepup.codefetcher.domain.coroutines.FeatureViewModel
import com.stepup.codefetcher.domain.coroutines.withLoading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.rx3.await
import javax.inject.Inject

@HiltViewModel
class FetchCodeViewModel2 @Inject constructor(
    private val service: CodeService,
    private val counterStorage: CounterStorage,
) : FeatureViewModel(), Container<State, Failure> {

    private val container = Container<_, Failure>(State())

    override val events = container.events
    override val state = container.state

    init {
        container.perform {
            counterStorage.currentValue.collect {
                updateState { copy(counter = it) }
            }
        }
    }

    fun fetchCode() = container.perform {
        withLoading(onLoading = { copy(isLoading = it) }) {
            service.getResponseCode().await()
        }
        counterStorage.increment()
    }

    override fun onUnhandledError(e: Throwable) = container.perform {
        emitEvent(Failure(e.errorMessage))
    }
}
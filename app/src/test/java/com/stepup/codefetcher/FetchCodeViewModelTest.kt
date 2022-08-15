package com.stepup.codefetcher

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeFalse
import org.junit.Test
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class FetchCodeViewModelTest {

    companion object {
        private const val path = "path"
        private const val code = "code"
    }

    @Test
    fun `when fetch requested, data is returned`() {
        val service = mockk<CodeService>()
        every { service.getPath() } returns Single.just(NextPath(path))
        every { service.getResponseCode(path) } returns Single.just(Response(path, code))
        val storage = mockk<CounterStorage>()
        every { storage.increment() } returns Completable.complete()
        every { storage.currentValue } returns Single.just(0).toObservable()
        val viewModel = FetchCodeViewModel(service, storage)
        with(
            viewModel.state
                .doOnSubscribe { viewModel.fetchCode() }
                .filter { it.code != null }.map { it.code!! }
                .test()
        ) {
            awaitCount(1)
            assertValue(code)
        }
        verifySequence {
            service.getPath()
            service.getResponseCode(path)
        }
    }

    @Test
    fun `when fetch requested, loading is updated`() {
        val service = mockk<CodeService>()
        every { service.getPath() } returns Single.just(NextPath(path)).delay(100, TimeUnit.MILLISECONDS)
        every { service.getResponseCode(path) } returns Single.just(Response(path, code))
        val storage = mockk<CounterStorage>()
        every { storage.increment() } returns Completable.complete()
        every { storage.currentValue } returns Single.just(0).toObservable()
        val viewModel = FetchCodeViewModel(service, storage)
        val loadingState = viewModel.state.map { it.isLoading }.distinct()
        loadingState.blockingFirst().shouldBeFalse()
        with(
            loadingState
                .doOnSubscribe { viewModel.fetchCode() }
                .take(2)
                .test()
        ) {
            await(5, TimeUnit.SECONDS)
            assertValues(true, false)
        }
    }

    @Test
    fun `when fetch requested, counter is updated`() {
        val service = mockk<CodeService>()
        every { service.getPath() } returns Single.just(NextPath(path))
        every { service.getResponseCode(path) } returns Single.just(Response(path, code))
        val storage = mockk<CounterStorage>()
        every { storage.increment() } returns Completable.complete()
        every { storage.currentValue } returns Single.just(0).toObservable()
        val viewModel = FetchCodeViewModel(service, storage)
        viewModel.fetchCode()
        viewModel.state.filter { it.code != null }
            .timeout(1, TimeUnit.SECONDS)
            .blockingFirst()
        verify(exactly = 1) { storage.increment() }
    }

    @Test
    fun `when fetch failed, error returned`() {
        val service = mockk<CodeService>()
        val message = "Message"
        every { service.getPath() } returns Single.just(NextPath(path))
        every { service.getResponseCode(path) } returns Single.error(
            HttpException(
                retrofit2.Response.error<Response>(
                    403,
                    Json.encodeToString(Error(message)).toResponseBody()
                )
            )
        )
        val storage = mockk<CounterStorage>()
        every { storage.increment() } returns Completable.complete()
        every { storage.currentValue } returns Single.just(0).toObservable()
        val viewModel = FetchCodeViewModel(service, storage)
        val error = viewModel.events
            .doOnSubscribe { viewModel.fetchCode() }
            .timeout(1, TimeUnit.SECONDS)
            .blockingFirst()
        error.message shouldBeEqualTo message
    }
}
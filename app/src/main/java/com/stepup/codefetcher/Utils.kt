package com.stepup.codefetcher

import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy

@Composable
fun <T : Any> Subscribe(
    observable: Observable<T>,
    onError: (Throwable) -> Unit = {},
    onComplete: () -> Unit = {},
    onNext: (T) -> Unit = {}
) = DisposableEffect(observable) {
    val disposable = observable.subscribeBy(onError, onComplete, onNext)
    onDispose { disposable.dispose() }
}

@Composable
fun ToolbarTitle(
    title: String,
    modifier: Modifier = Modifier
) = CompositionLocalProvider(LocalContentAlpha provides 1f) {
    Text(title, modifier)
}
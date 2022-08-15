package com.stepup.codefetcher.domain.rx

import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject

interface Container<State : Any, Event : Any> {

    val state: Observable<State>
    val events: Flowable<Event>

    interface Mutator<State, Event> {
        val current: State
        fun updateState(mutation: State.() -> State)
        fun emitEvent(event: Event)
    }
}

interface MutableContainer<State : Any, Event : Any> : Container<State, Event>,
    Container.Mutator<State, Event>

@Suppress("FunctionName")
fun <State : Any, Event : Any> Container(initialState: State): MutableContainer<State, Event> {
    return object : MutableContainer<State, Event>, Container.Mutator<State, Event>  {
        val mutableEvents = PublishSubject.create<Event>()
        override val state = BehaviorSubject.createDefault(initialState)
        override val events = mutableEvents.toFlowable(BackpressureStrategy.BUFFER)
        override fun emitEvent(event: Event) = mutableEvents.onNext(event)
        override val current: State get() = state.value!!
        override fun updateState(mutation: State.() -> State) = synchronized(state) {
            state.onNext(current.mutation())
        }
    }
}

fun <T : Any> Single<T>.doOnLoading(action: (isLoading: Boolean) -> Unit): Single<T> {
    return doOnSubscribe { action(true) }.doOnTerminate { action(false) }
}
package com.stepup.codefetcher.domain.coroutines

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

interface StateContainer<out State> {

    val state: StateFlow<State>

    interface Mutator<State> {
        val current: State
        fun updateState(mutation: State.() -> State)
    }
}

interface EventsContainer<out Event> {

    val events: Flow<Event>

    interface Mutator<in Event> {
        suspend fun emitEvent(event: Event)
    }
}

interface Container<State, Event> : StateContainer<State>, EventsContainer<Event> {

    interface Mutator<State, in Event> : StateContainer.Mutator<State>, EventsContainer.Mutator<Event>
}

interface MutableContainer<State, Event> : Container<State, Event>, Container.Mutator<State, Event>

@Suppress("FunctionName")
fun <State, Event> Container(
    initialState: State,
    eventsChannelCapacity: Int = Channel.BUFFERED
): MutableContainer<State, Event> = object : MutableContainer<State, Event> {

    val mutableEvents = Channel<Event>(eventsChannelCapacity)
    override val state = MutableStateFlow(initialState)
    override val events = mutableEvents.receiveAsFlow()

    override val current: State get() = state.value
    override fun updateState(mutation: State.() -> State) = state.update(mutation)
    override suspend fun emitEvent(event: Event) = mutableEvents.send(event)
}


inline fun <State, Event> Container.Mutator<State, Event>.withLoading(
    crossinline onLoading: State.(isLoading: Boolean) -> State,
    block: () -> Unit
) {
    updateState { onLoading(true) }
    try {
        block()
    } finally {
        updateState { onLoading(false) }
    }
}

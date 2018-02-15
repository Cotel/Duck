package com.cotel.duck

class Store(private val reducers: List<Duck> = emptyList()) {

    private var state: Map<String, Any>
    private val subscribers: MutableCollection<Subscriptor> = mutableListOf()

    init {
        state = reducers
                .map { it as Reducer<Any, Action> }
                .map { Pair(it.identifier, it.initialState) }
                .toMap()
    }

    fun subscribe(sub: Subscriptor) {
        subscribers.add(sub)
    }

    fun unsubscribe(sub: Subscriptor) {
        subscribers.remove(sub)
    }

    fun dispatch(action: Action) {
        state = reducers
                .map { it as Reducer<Any, Action> }
                .map { Pair(it.identifier, it.reduce(state[it.identifier]!!, action)) }
                .toMap()

        subscribers
                .forEach { it.listen(state) }
    }
}

interface Subscriptor {
    val store: Store
    fun listen(newState: Map<String, Any>)
}
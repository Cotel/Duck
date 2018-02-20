package com.cotel.duck

interface Dispatcher {
    fun dispatch(action: Action)
}

class Store(private val reducers: Set<Duck>) : Dispatcher {

    lateinit private var middlewareChain: Middleware
    private val subscribers: MutableCollection<Subscriptor> = mutableListOf()

    var state: Map<String, Any>
        private set

    init {
        middlewareChain = BaseMiddleware(this)

        state = reducers
                .map { it as Reducer<Any, Action> }
                .map { Pair(it.identifier, it.initialState) }
                .toMap()
    }

    fun setMiddleWareChain(middleware: Middleware) {
        middlewareChain = middleware
    }

    fun subscribe(sub: Subscriptor) {
        subscribers.add(sub)
    }

    fun unsubscribe(sub: Subscriptor) {
        subscribers.remove(sub)
    }

    override fun dispatch(action: Action) {
        middlewareChain!!.dispatch(action)
    }

    internal fun reduceAction(action: Action) {
        state = reducers
                .map { it as Reducer<Any, Action> }
                .map { Pair(it.identifier, it.reduce(state[it.identifier]!!, action)) }
                .toMap()

        subscribers
                .forEach { it.onStateChanged() }
    }
}

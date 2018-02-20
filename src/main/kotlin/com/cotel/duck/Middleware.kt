package com.cotel.duck

interface Middleware : Dispatcher {
    val next: Dispatcher
}

class BaseMiddleware(override val next: Store) : Middleware {
    override fun dispatch(action: Action) {
        next.reduceAction(action)
    }
}
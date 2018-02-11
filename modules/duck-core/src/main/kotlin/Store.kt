class Store(private val reducers: Map<String, Reducer> = emptyMap()) {

    private val subscribers: MutableCollection<Subscriptor> = mutableListOf()

    fun subscribe(sub: Subscriptor) {
        subscribers.add(sub)
    }

    fun unsubscribe(sub: Subscriptor) {
        subscribers.remove(sub)
    }

    fun dispatch(action: Action) {
        val newState = reducers
                .mapValues { it.value as Duck<Action, Any> }
                .mapValues { (_, duck: Duck<Action, Any>) -> duck.reduce(action) }

        subscribers
                .forEach { it.listen(newState) }
    }
}

interface Subscriptor {
    val store: Store
    fun listen(newState: Map<String, Any>)
}
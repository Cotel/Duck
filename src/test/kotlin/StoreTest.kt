import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

class StoreTest : StringSpec({
    "Store should notify subscriptors on state change" {
        var timesCalled = 0
        val subscriptor = object : Subscriptor {
            override val store: Store = testStore

            override fun listen(newState: Map<String, Any>) {
                timesCalled++
            }
        }
        testStore.subscribe(subscriptor)

        subscriptor.store.dispatch(CounterActions.Increment)
        subscriptor.store.dispatch(CounterActions.Decrement)
        subscriptor.store.dispatch(CounterActions.Increment)
        subscriptor.store.dispatch(CounterActions.Reset)

        timesCalled shouldBe 4
    }

    "Store reducers should update if they share actions" {
        var currentCounter = 0
        var currentWord = ""
        val subscriptor = object : Subscriptor {
            override val store: Store = testStore

            override fun listen(newState: Map<String, Any>) {
                currentCounter = newState["counter"] as Int
                currentWord = newState["word"] as String
            }
        }
        testStore.subscribe(subscriptor)

        testStore.dispatch(CounterActions.Increment)
        testStore.dispatch(CounterActions.Increment)
        testStore.dispatch(CounterActions.Decrement)

        currentCounter shouldBe 1
        currentWord shouldBe "a"
    }

})

sealed class CounterActions : Action {
    object Increment : CounterActions()
    object Decrement : CounterActions()
    object Reset : CounterActions()
}

class CounterReducer : Reducer<Int, CounterActions> {
    override val identifier: String = "counter"
    override val initialState: Int
        get() = 0

    override fun reduce(state: Int, action: CounterActions): Int = when (action) {
        CounterActions.Increment -> state + 1
        CounterActions.Decrement -> state - 1
        CounterActions.Reset -> 0
    }
}

class StringReducer : Reducer<String, CounterActions> {
    override val identifier: String = "word"
    override val initialState: String
        get() = ""

    override fun reduce(state: String, action: CounterActions): String = when (action) {
        CounterActions.Increment -> state + "a"
        CounterActions.Decrement -> state.drop(1)
        CounterActions.Reset -> ""
    }
}

val testStore = Store(listOf(
        CounterReducer(),
        StringReducer()
))

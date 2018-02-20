import com.cotel.duck.*
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import java.time.Duration
import kotlin.concurrent.thread

class StoreTest : StringSpec({
    "Store should notify subscriptors on state change" {
        var timesCalled = 0
        val subscriptor = object : Subscriptor {
            override val store: Store = testStore

            override fun onStateChanged() {
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
        testStore.dispatch(CounterActions.Increment)
        testStore.dispatch(CounterActions.Increment)
        testStore.dispatch(CounterActions.IncrementBy(2))
        testStore.dispatch(CounterActions.Decrement)

        testStore.state["counter"] shouldBe 3
        testStore.state["word"] shouldBe "aaa"
    }

    "Store middleware can modify actions" {
        val enhancedTestStore = Store(setOf(
            CounterReducer()
        ))
        val middleware = PayloadIncrementMiddleware(PayloadIncrementMiddleware(BaseMiddleware(enhancedTestStore)))
        enhancedTestStore.setMiddleWareChain(middleware)

        enhancedTestStore.dispatch(CounterActions.Increment)

        enhancedTestStore.state["counter"] shouldBe 3
    }

    "Store middlewares can dispatch asynchronous actions" {
        val enhancedTestStore = Store(setOf(
                CounterReducer()
        ))
        val middleware = PauseMiddleware(BaseMiddleware(enhancedTestStore))
        enhancedTestStore.setMiddleWareChain(middleware)

        enhancedTestStore.dispatch(CounterActions.Increment)

        enhancedTestStore.state["counter"] shouldBe 0
        Thread.sleep(Duration.ofSeconds(4).toMillis())
        enhancedTestStore.state["counter"] shouldBe 1
    }

})

sealed class CounterActions : Action {
    object Increment : CounterActions()
    class IncrementBy(override val payload: Int) : CounterActions(), PayloadAction<Int>
    object Decrement : CounterActions()
    object Reset : CounterActions()
}

class CounterReducer : Reducer<Int, CounterActions> {
    override val identifier: String = "counter"
    override val initialState: Int
        get() = 0

    override fun reduce(state: Int, action: CounterActions): Int = when (action) {
        CounterActions.Increment -> state + 1
        is CounterActions.IncrementBy -> state + action.payload
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
        is CounterActions.IncrementBy -> state + "a".repeat(action.payload)
        CounterActions.Decrement -> state.drop(1)
        CounterActions.Reset -> ""
    }
}

class PayloadIncrementMiddleware(override val next: Dispatcher) : Middleware {
    override fun dispatch(action: Action) {
        when (action) {
            CounterActions.Increment -> next.dispatch(CounterActions.IncrementBy(2))
            is CounterActions.IncrementBy -> next.dispatch(CounterActions.IncrementBy(action.payload + 1))
            else -> next.dispatch(action)
        }
    }
}

class PauseMiddleware(override val next: Dispatcher) : Middleware {
    override fun dispatch(action: Action) {
        next.dispatch(CounterActions.IncrementBy(0))
        thread(start = true) {
            Thread.sleep(Duration.ofSeconds(3).toMillis())
            next.dispatch(action)
        }
    }
}

val testStore = Store(setOf(
        CounterReducer(),
        StringReducer()
))

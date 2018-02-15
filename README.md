# Duck

### A Redux implementation in Kotlin

#### Quick start

Add this to your _build.gradle_

```gradle

allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    compile 'com.github.Cotel:Duck:v0.1.1'
}

```

And you are ready to go!

#### Sample

```kotlin
sealed class CounterActions : Action {
    object Increment : CounterActions()
    object Decrement : CounterActions()
    object Reset : CounterActions()
}

class CounterReducer : Reducer<Int, CounterActions> {
    override val identifier: String = "counter"
    override val initialState: Int = 0

    override fun reduce(state: Int, action: CounterActions): Int = when (action) {
        CounterActions.Increment -> state + 1
        CounterActions.Decrement -> state - 1
        CounterActions.Reset -> 0
    }
}

class Component(override val store: Store) : Subscriptor {
    override fun listen(newState: Map<String, Any>) {
        println("Counter: ${newState['counter']}")
    }
}

val store = Store(listOf(
        CounterReducer()
))

fun main(vararg args: String) {
    val component = Component(store)
    store.subscribe(component)

    store.dispatch(CounterActions.Increment)
}
```

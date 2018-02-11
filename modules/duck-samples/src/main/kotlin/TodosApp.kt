import java.util.*
import kotlin.properties.Delegates

data class Todo(val title: String,
                val finished: Boolean,
                val id: UUID = UUID.randomUUID())

sealed class TodosActions : Action {
    class ListTodos(override val payload: List<Todo>) : TodosActions(), PayloadAction<List<Todo>>
    class AddTodo(override val payload: Todo) : TodosActions(), PayloadAction<Todo>
    class RemoveTodo(override val payload: Todo) : TodosActions(), PayloadAction<Todo>
    class ToggleTodo(override val payload: Todo) : TodosActions(), PayloadAction<Todo>
}

class TodosReducer(override var state: List<Todo>) : Duck<TodosActions, List<Todo>> {
    override fun reduce(action: TodosActions): List<Todo> = when (action) {
        is TodosActions.ListTodos -> action.payload
        is TodosActions.AddTodo -> state + action.payload
        is TodosActions.RemoveTodo -> state.minus(action.payload)
        is TodosActions.ToggleTodo -> state.map { if (it.id == action.payload.id) it.copy(finished = !it.finished) else it }
    }
}

class TodosController(override val store: Store) : Subscriptor {
    var state by Delegates.observable(emptyList<Todo>()) { _, _, new ->
        println(new)
    }

    fun addTodo() {
        val todo = Todo("Test", false)
        store.dispatch(TodosActions.AddTodo(todo))
    }

    override fun listen(newState: Map<String, Any>) {
        state = newState["todos"] as List<Todo>
    }

}

fun main(vararg args: String) {
    val rootReducer = mapOf<String, Reducer>(
            "todos" to TodosReducer(emptyList())
    )
    val store = Store(rootReducer)

    val controller = TodosController(store)
    store.subscribe(controller)

    controller.addTodo()
}
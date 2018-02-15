interface Duck

interface Reducer<S, in A: Action> : Duck {
    val identifier: String
    val initialState: S

    fun reduce(state: S, action: A): S
}
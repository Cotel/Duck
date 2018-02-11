interface Reducer

interface Duck<in A: Action, S> : Reducer {
    var state: S
    fun reduce(action: A): S
}
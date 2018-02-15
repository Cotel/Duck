package com.cotel.duck

interface Subscriptor {
    val store: Store
    fun listen(newState: Map<String, Any>)
}
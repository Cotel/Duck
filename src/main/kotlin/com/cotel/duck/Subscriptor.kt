package com.cotel.duck

interface Subscriptor {
    val store: Store
    fun onStateChanged()
}
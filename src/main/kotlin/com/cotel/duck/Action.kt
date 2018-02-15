package com.cotel.duck

interface Action

interface PayloadAction<out T> : Action {
    val payload: T
}
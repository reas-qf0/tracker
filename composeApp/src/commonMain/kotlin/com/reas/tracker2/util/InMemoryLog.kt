package com.reas.tracker2.util

import com.reas.tracker2.platform.IS_DEBUG
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

class InMemoryLog {
    companion object {
        private const val DEFAULT_CAPACITY = 1000
    }

    private val logs = mutableMapOf<String, MutableSharedFlow<String>>()

    operator fun get(tag: String) = logs.getOrPut(tag) {
        MutableSharedFlow(replay = DEFAULT_CAPACITY, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    }

    suspend fun log(tag: String, message: () -> String) {
        if (!IS_DEBUG) return
        get(tag).emit(message())
    }
}
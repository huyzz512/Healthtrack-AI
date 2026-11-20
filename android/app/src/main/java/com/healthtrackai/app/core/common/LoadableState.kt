package com.healthtrackai.app.core.common

sealed interface LoadableState<out T> {
    data object Loading : LoadableState<Nothing>
    data object Empty : LoadableState<Nothing>
    data class Content<T>(val value: T) : LoadableState<T>
    data class Error(val message: String) : LoadableState<Nothing>
    data class Offline<T>(
        val cachedValue: T?,
        val message: String,
    ) : LoadableState<T>

    data class Unavailable(val reason: String) : LoadableState<Nothing>
}

package com.storiq.core.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ErrorHandlingViewModel : ViewModel() {

    private val supervisorJob = SupervisorJob()
    override val viewModelScope: CoroutineScope
        get() = CoroutineScope(supervisorJob + CoroutineExceptionHandler { _, throwable ->
            // Log error
            android.util.Log.e("StorIQ", "Unhandled exception in ViewModel", throwable)
        })

    fun handleError(throwable: Throwable, userMessage: String? = null): ErrorResult {
        val message = userMessage ?: throwable.localizedMessage ?: "An error occurred"
        return ErrorResult(
            success = false,
            errorMessage = message,
            throwable = throwable
        )
    }

    fun handleSuccess<T>(data: T): ErrorResult<T> {
        return ErrorResult(
            success = true,
            data = data
        )
    }
}

sealed class ErrorResult<out T> {
    data class Success<T>(val data: T) : ErrorResult<T>()
    data class Error(
        val errorMessage: String,
        val throwable: Throwable? = null
    ) : ErrorResult<Nothing>()

    val isSuccess: Boolean
        get() = this is Success<*>

    val isError: Boolean
        get() = this is Error
}

inline fun <T> safeExecute(
    onError: (Throwable) -> Unit = { },
    block: () -> T
): T? {
    try {
        return block()
    } catch (e: Exception) {
        onError(e)
        return null
    }
}

inline fun <T> safeExecuteAsync(
    scope: CoroutineScope = kotlinx.coroutines.GlobalScope,
    onError: (Throwable) -> Unit = { },
    block: suspend () -> T
): Job {
    return scope.launch {
        try {
            block()
        } catch (e: Exception) {
            onError(e)
        }
    }
}

class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val throwable: Throwable) : Result<Nothing>()

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun <T> error(throwable: Throwable): Result<T> = Error(throwable)
    }

    val isSuccess: Boolean
        get() = this is Success<*>

    val isError: Boolean
        get() = this is Error

    fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Result.success(transform(data))
            is Error -> Result.error(throwable)
        }
    }

    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> {
        return when (this) {
            is Success -> transform(data)
            is Error -> Result.error(throwable)
        }
    }

    fun getOrElse(default: T): T {
        return when (this) {
            is Success -> data
            is Error -> default
        }
    }
}